package ascelion.microprofile.config.cdi;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.enterprise.inject.spi.WithAnnotations;

import ascelion.cdi.bean.BeanAttributesModifier;
import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.microprofile.config.ConfigValue;
import ascelion.microprofile.config.eval.ExpressionConfig;
import ascelion.microprofile.config.util.ConfigUtil;

import static java.util.stream.Collectors.toList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigExtension implements Extension {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigExtension.class);

	private final Set<Type> types = new HashSet<>();
	private final Map<Class<?>, Collection<AnnotatedMethod<?>>> setters = new HashMap<>();

	void beforeBeanDiscovery(BeanManager bm, @Observes BeforeBeanDiscovery event) {
		event.addQualifier(AnnotatedTypeModifier.makeQualifier(bm.createAnnotatedType(ConfigValue.class)));
	}

	@SuppressWarnings("unchecked")
	<X> void collectConfigValues(BeanManager bm,
			@Observes @WithAnnotations(ConfigValue.class) ProcessAnnotatedType<X> event) {
		final ExpressionConfig expConfig = new ExpressionConfig(ConfigUtil.getConfig(bm));
		final ConfigInject<X> inject = new ConfigInject<>(expConfig, event.getAnnotatedType());
		final Set<ConfigValue> cvs = inject.values();

		if (cvs.size() > 0) {
			final AnnotatedType<X> type = inject.type();

			LOG.info("Updated type {}", type);

			event.setAnnotatedType(type);

			final List<AnnotatedMethod<? super X>> methods = type.getMethods().stream()
					.filter(m -> m.isAnnotationPresent(ConfigSet.class))
					.collect(toList());

			if (methods.size() > 0) {
				this.setters.put(type.getJavaClass(), (Collection) methods);
			}
		}
	}

	<T, X> void processInjectionPoint(BeanManager bm, @Observes ProcessInjectionPoint<T, X> event) {
		final InjectionPoint ijp = event.getInjectionPoint();

		if (ijp.getAnnotated().isAnnotationPresent(ConfigValue.class)) {
			LOG.debug("Need to produce: {}", ijp.getType());

			this.types.add(ijp.getType());
		}
	}

	<X> void processInjectionTarget(BeanManager bm, @Observes ProcessInjectionTarget<X> event) {
		final Collection<AnnotatedMethod<?>> methods = this.setters.get(event.getAnnotatedType().getJavaClass());

		if (methods != null) {
			final InjectionTarget<X> it = event.getInjectionTarget();

			LOG.info("Overring injection of {}", it);

			event.setInjectionTarget(new ConfigInjectionTarget<>(bm, it, methods));
		}
	}

	void afterBeanDiscovery(BeanManager bm, @Observes AfterBeanDiscovery event) {
		if (this.types.isEmpty()) {
			return;
		}

		LOG.info("Adding @ConfigValue producer(s) for {}", this.types);

		final AnnotatedType<ConfigValueProd> type = bm.createAnnotatedType(ConfigValueProd.class);
		final BeanAttributes<ConfigValueProd> beanAttributes = bm.createBeanAttributes(type);
		final InjectionTarget<ConfigValueProd> beanInjTarget = bm.createInjectionTarget(type);
		final Bean<ConfigValueProd> bean = bm.createBean(beanAttributes, ConfigValueProd.class, (Bean<ConfigValueProd> b) -> beanInjTarget);

		event.addBean(bean);

		final AnnotatedMethod<? super ConfigValueProd> method = type.getMethods().stream()
				.filter(m -> m.isAnnotationPresent(Produces.class))
				.findFirst()
				.get();

		final ProducerFactory<ConfigValueProd> prodFactory = bm.getProducerFactory(method, bean);
		final BeanAttributesModifier<?> prodAttributes = BeanAttributesModifier.create(bm.createBeanAttributes(method));

		prodAttributes.types().addAll(this.types);

		final Bean<?> prodBean = bm.createBean(prodAttributes.get(), ConfigValueProd.class, prodFactory);

		event.addBean(prodBean);
	}
}
