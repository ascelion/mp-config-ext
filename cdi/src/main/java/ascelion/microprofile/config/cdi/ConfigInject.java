package ascelion.microprofile.config.cdi;

import java.beans.Introspector;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;

import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.cdi.metadata.AnnotatedTypeModifier.Modifier;
import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;
import ascelion.microprofile.config.eval.ExpressionConfig;

import static java.lang.String.format;
import static java.util.Collections.newSetFromMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConfigInject<T> {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigInject.class);

	private final ExpressionConfig config;
	private final AnnotatedTypeModifier<T> tmod;
	private final String prefix;
	private final String typeName;
	private final boolean hasPrefix;

	private final Set<ConfigValue> values = new HashSet<>();

	private final Set<AnnotatedMethod<T>> methods = newSetFromMap(new IdentityHashMap<>());
	private final Set<AnnotatedField<T>> fields = newSetFromMap(new IdentityHashMap<>());

	ConfigInject(Config config, AnnotatedType<T> type) {
		this.config = new ExpressionConfig(config);
		this.tmod = AnnotatedTypeModifier.create(type);
		this.typeName = type.getJavaClass().getCanonicalName();
		this.prefix = ofNullable(type.getAnnotation(ConfigPrefix.class))
				.map(ConfigPrefix::value)
				.orElse(null);
		this.hasPrefix = this.prefix != null && this.prefix.length() > 0;
	}

	Set<ConfigValue> values() {
		if (this.values.isEmpty()) {
			processCallables(this.tmod.getCallables());
			processFields(this.tmod.getFields());
		}

		return this.values;
	}

	AnnotatedType<T> type() {
		return this.tmod.get();
	}

	Set<AnnotatedField<T>> fields() {
		return unmodifiableSet(this.fields);
	}

	Set<AnnotatedMethod<T>> methods() {
		return unmodifiableSet(this.methods);
	}

	@SuppressWarnings("unchecked")
	private void processCallables(Set<AnnotatedCallable<? super T>> callables) {
		callables.stream()
				.filter(callable -> !callable.isAnnotationPresent(Produces.class))
				.filter(callable -> callable.getParameters().size() > 0)
				.filter(callable -> callable.getParameters().stream().anyMatch(p -> p.isAnnotationPresent(ConfigValue.class)))
				.forEach(callable -> processCallable((AnnotatedCallable<T>) callable));
	}

	private void processCallable(AnnotatedCallable<T> callable) {
		final Executable executable = (Executable) callable.getJavaMember();
		final List<AnnotatedParameter<T>> parameters = callable.getParameters();

		if (parameters.size() == 1) {
			final AnnotatedParameter<? super T> parameter = parameters.get(0);
			final ConfigValue cval = parameter.getAnnotation(ConfigValue.class);
			final Modifier<T> pmod = this.tmod.executableParam(executable, 0);
			String name = executable.getName();

			if (name.startsWith("set")) {
				if (cval.required()) {
					this.tmod.executable(executable)
							.add(InjectLiteral.INSTANCE);
				} else {
					this.tmod.executable(executable)
							.remove(Inject.class);

					LOG.debug("Removing injection from setter in {}", executable);
				}

				this.methods.add((AnnotatedMethod<T>) callable);

				name = Introspector.decapitalize(name.substring(3));
			} else {
				this.tmod.executable(executable)
						.add(InjectLiteral.INSTANCE);
			}

			updateAnnotation(cval, pmod, name);
		} else {
			this.tmod.executable(executable)
					.add(InjectLiteral.INSTANCE);

			for (int k = 0; k < parameters.size(); k++) {
				final AnnotatedParameter<? super T> parameter = parameters.get(k);

				if (parameter.isAnnotationPresent(ConfigValue.class)) {
					final ConfigValue cval = parameter.getAnnotation(ConfigValue.class);
					final Modifier<T> pmod = this.tmod.executableParam(executable, k);

					updateAnnotation(cval, pmod, "");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void processFields(Set<AnnotatedField<? super T>> fields) {
		fields.stream()
				.filter(field -> !field.isAnnotationPresent(Produces.class))
				.filter(field -> field.isAnnotationPresent(ConfigValue.class))
				.forEach(field -> processField((AnnotatedField<T>) field));
	}

	private void processField(AnnotatedField<T> annotated) {
		final ConfigValue cval = annotated.getAnnotation(ConfigValue.class);
		final Modifier<T> fmod = this.tmod.field(annotated);
		final Field field = annotated.getJavaMember();

		if (cval.required()) {
			fmod.add(InjectLiteral.INSTANCE);
		}

		this.fields.add(annotated);

		updateAnnotation(cval, fmod, field.getName());
	}

	private ConfigValue updateAnnotation(ConfigValue cval, Modifier<T> amod, String name) {
		final String prop = cval.value();
		final int varIx = this.config.variableIndex(prop);

		amod.remove(ConfigValue.class);

		final StringBuilder expr = new StringBuilder();

		if (prop.isEmpty()) {
			if (name.isEmpty()) {
				throw new DeploymentException(format("Configuration name is required for %s", amod.and().get()));
			}

			if (varIx < 0 && cval.usePrefix()) {
				if (this.hasPrefix) {
					expr.append(this.prefix);
				} else {
					expr.append(this.typeName);
				}

				expr.append(".");
			}

			expr.append(name);
		} else {
			if (varIx < 0 && cval.usePrefix() && this.hasPrefix) {
				expr.append(this.prefix);
				expr.append(".");
			}

			expr.append(prop);
		}

		cval = new ConfigValueLiteral(expr.toString(), cval);

		this.values.add(cval);

		amod.add(cval);

		LOG.debug("Set {} to {}", expr, amod);

		return cval;
	}
}
