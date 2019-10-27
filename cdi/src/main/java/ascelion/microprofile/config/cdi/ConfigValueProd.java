package ascelion.microprofile.config.cdi;

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
		final ConfigValue cval = ijp.getAnnotated().getAnnotation(ConfigValue.class);
		final Class type = (Class<?>) ijp.getType();
		final String prop = cval.value();

		if (cval.required()) {
			return this.config.getValue(prop, type);
		} else {
			return this.config.getOptionalValue(prop, type)
					.orElseGet(() -> Primitives.toDefault(type));
		}
	}
}
