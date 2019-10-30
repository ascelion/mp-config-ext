package ascelion.microprofile.config.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;

import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.microprofile.config.ConfigValue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unchecked")
class ConfigValueProd {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigInject.class);

	private final BeanManager bm;
	private final Config config;

	interface Adder<C, T> {
		void apply(C c, T t, String k);
	}

	@ConfigValue("")
	Object produceType(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final String property = annotation.value();
		final Class<?> type = ConfigValueLiteral.actualType(ijp.getType(), annotation);

		return getValue(type, property, annotation.required());
	}

	@Produces
	@ConfigValue("")
	Properties produceProperties(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);

		return produceCollection(String.class, annotation, Properties::new, (c, v, k) -> c.setProperty(k, v));
	}

	@Produces
	@ConfigValue("")
	<T> Map<String, T> produceMap(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class<T> type = (Class<T>) (((ParameterizedType) ijp.getType()).getActualTypeArguments()[1]);

		return produceCollection(type, annotation, HashMap::new, (c, v, k) -> c.put(k, v));
	}

	@Produces
	@Typed(Collection.class)
	@ConfigValue("")
	<T> Collection<T> produceCollection(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class<T> type = (Class<T>) (((ParameterizedType) ijp.getType()).getActualTypeArguments()[0]);

		return produceCollection(type, annotation, ArrayList::new, (c, v, k) -> c.add(v));
	}

	@Produces
	@Typed(List.class)
	@ConfigValue("")
	<T> List<T> produceList(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class<T> type = (Class<T>) (((ParameterizedType) ijp.getType()).getActualTypeArguments()[0]);

		return produceCollection(type, annotation, ArrayList::new, (c, v, k) -> c.add(v));
	}

	@Produces
	@Typed(Set.class)
	@ConfigValue("")
	<T> Set<T> produceSet(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class<T> type = (Class<T>) (((ParameterizedType) ijp.getType()).getActualTypeArguments()[0]);

		return produceCollection(type, annotation, HashSet::new, (c, v, k) -> c.add(v));
	}

	@Produces
	@Typed(Optional.class)
	@ConfigValue("")
	<T> Optional<T> produceOptional(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class<T> type = (Class<T>) (((ParameterizedType) ijp.getType()).getActualTypeArguments()[0]);
		final String property = annotation.value();

		return Optional.ofNullable(getValue(type, property, annotation.required()));
	}

	@SneakyThrows
	<T> void inject(T instance, AnnotatedField<T> annotated) {
		final ConfigValue cval = annotated.getAnnotation(ConfigValue.class);
		final String prop = cval.value();
		final Type type = annotated.getBaseType();
		final Object value = getValue(type, prop, cval);

		if (value != null) {
			final Field field = annotated.getJavaMember();

			LOG.debug("Invoking setter {}", field);

			field.setAccessible(true);
			field.set(instance, value);
		}
	}

	@SneakyThrows
	<T> void inject(T instance, AnnotatedMethod<T> annotated) {
		final AnnotatedParameter<T> param = annotated.getParameters().get(0);
		final ConfigValue cval = param.getAnnotation(ConfigValue.class);
		final String prop = cval.value();
		final Type type = param.getBaseType();
		final Object value = getValue(type, prop, cval);

		if (value != null) {
			final Method method = annotated.getJavaMember();

			LOG.debug("Invoking setter {}", annotated);

			method.setAccessible(true);
			method.invoke(instance, value);
		}
	}

	private <C, T> C produceCollection(Class<T> type, ConfigValue annotation, Supplier<C> sup, Adder<C, T> add) {
		final String property = annotation.value() + ".";
		final C col = ConfigValueLiteral.createType(annotation, sup);

		LOG.debug("Created {} for {}", col.getClass(), property);

		for (final String name : this.config.getPropertyNames()) {
			if (name.startsWith(property)) {
				LOG.debug("Adding {}", name);

				final T value = getValue(type, name, annotation.required());

				add.apply(col, value, name.substring(property.length()));
			}
		}

		return col;
	}

	private <T> T getValue(Type type, String property, ConfigValue annotation) {
		if (type instanceof Class) {
			if (type == Properties.class) {
				return (T) produceCollection(String.class, annotation, Properties::new, (c, v, k) -> c.setProperty(k, v));
			}

			return (T) getValue((Class) type, property, annotation.required());
		}

		if (type instanceof ParameterizedType) {
			final ParameterizedType parType = (ParameterizedType) type;
			final Class<?> rawClass = (Class<?>) parType.getRawType();

			if (Map.class.isAssignableFrom(rawClass)) {
				final Class<?> itemType = (Class<?>) parType.getActualTypeArguments()[1];

				return (T) produceCollection(itemType, annotation, HashMap::new, (c, v, k) -> c.put(k, v));
			}
			if (Set.class.isAssignableFrom(rawClass)) {
				final Class<?> itemType = (Class<?>) parType.getActualTypeArguments()[0];

				return (T) produceCollection(itemType, annotation, HashSet::new, (c, v, k) -> c.add(v));
			}
			if (Collection.class.isAssignableFrom(rawClass)) {
				final Class<?> itemType = (Class<?>) parType.getActualTypeArguments()[0];

				return (T) produceCollection(itemType, annotation, ArrayList::new, (c, v, k) -> c.add(v));
			}
		}

		throw new UnsupportedOperationException();
	}

	private <T> T getValue(Class<T> type, String property, boolean required) {
		if (type.isAnnotationPresent(ConfigValue.class)) {
			return buildType(type, property, required);
		}

		if (required) {
			return this.config.getValue(property, type);
		}

		return this.config.getOptionalValue(property, type)
				.orElseGet(() -> Primitives.toDefault(type));
	}

	private <T> T buildType(Class<T> type, String property, boolean required) {
		final AnnotatedTypeModifier<T> tmod = AnnotatedTypeModifier.create(this.bm.createAnnotatedType(type));

		tmod.type().add(new ConfigPrefixLiteral(property));

		final T instance = new Unmanaged<>(this.bm, type)
				.newInstance()
				.produce()
				.inject()
				.postConstruct()
				.get();

		final ConfigInject<T> ci = new ConfigInject<>(this.config, tmod.get());

		if (ci.values().size() > 0) {
			ci.fields().forEach(f -> inject(instance, f));
			ci.methods().forEach(m -> inject(instance, m));
		}

		return instance;
	}

}
