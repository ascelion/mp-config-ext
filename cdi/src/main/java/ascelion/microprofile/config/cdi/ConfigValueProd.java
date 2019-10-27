package ascelion.microprofile.config.cdi;

import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.microprofile.config.ConfigValue;

import org.eclipse.microprofile.config.Config;

class ConfigValueProd {
	@Inject
	private Config config;

	@Produces
	@ConfigValue("")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Object produceType(InjectionPoint ijp) {
		final ConfigValue annotation = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final String property = annotation.value();
		final Class type = (Class<?>) ijp.getType();

		if (type == Optional.class) {
			return this.config.getOptionalValue(property, type);
		} else if (annotation.required()) {
			return this.config.getValue(property, type);
		} else {
			return this.config.getOptionalValue(property, type)
					.orElseGet(() -> Primitives.toDefault(type));
		}
	}
}
