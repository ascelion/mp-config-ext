package ascelion.microprofile.config.cdi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import ascelion.microprofile.config.ConfigValue;
import ascelion.microprofile.config.util.ConfigInstance;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
class ConfigInjectionTarget<T> implements InjectionTarget<T> {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigInjectionTarget.class);

	private final BeanManager bm;
	private final InjectionTarget<T> delegate;
	private final Collection<AnnotatedMethod<?>> methods;

	@Override
	public T produce(CreationalContext<T> ctx) {
		return this.delegate.produce(ctx);
	}

	@Override
	public void inject(T instance, CreationalContext<T> ctx) {
		this.delegate.inject(instance, ctx);

		this.methods.forEach(m -> configure(instance, m));
	}

	@Override
	public void postConstruct(T instance) {
		this.delegate.postConstruct(instance);
	}

	@Override
	public void dispose(T instance) {
		this.delegate.dispose(instance);
	}

	@Override
	public void preDestroy(T instance) {
		this.delegate.preDestroy(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return this.delegate.getInjectionPoints();
	}

	private void configure(T instance, AnnotatedMethod<?> annotated) {
		final AnnotatedParameter<?> param = annotated.getParameters().get(0);
		final ConfigValue cval = param.getAnnotation(ConfigValue.class);
		final String prop = cval.value();
		final Class<?> type = param.getJavaParameter().getType();

		ConfigInstance.get(this.bm)
				.getOptionalValue(prop, type)
				.ifPresent(value -> {
					try {
						final Method method = annotated.getJavaMember();

						LOG.debug("Invoking setter {}", method);

						method.setAccessible(true);
						method.invoke(instance, value);
					} catch (final IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (final InvocationTargetException e) {
						throw new RuntimeException(e.getCause());
					}
				});
	}

}
