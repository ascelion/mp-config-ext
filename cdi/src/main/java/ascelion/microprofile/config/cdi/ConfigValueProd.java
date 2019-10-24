package ascelion.microprofile.config.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.microprofile.config.ConfigValue;
import ascelion.microprofile.config.util.ConfigUtil;

import org.eclipse.microprofile.config.Config;

@Dependent
class ConfigValueProd {
	private Config config;

	@Produces
	@Dependent
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

	@PostConstruct
	private void initConfig() {
		this.config = ConfigUtil.getConfig();
	}
}
