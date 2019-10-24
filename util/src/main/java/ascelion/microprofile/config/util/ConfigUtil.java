package ascelion.microprofile.config.util;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtil {
	static public Config getConfig(BeanManager bm) {
		final Set<Bean<?>> beans;

		try {
			beans = bm.getBeans(Config.class);
		} catch (final IllegalStateException e) {
			return ConfigProvider.getConfig();
		}

		if (beans.isEmpty()) {
			return ConfigProvider.getConfig();
		}
		if (beans.size() > 1) {
			throw new AmbiguousResolutionException("Ambigous bean definition for Config");
		}

		@SuppressWarnings("unchecked")
		final Bean<Config> bean = (Bean<Config>) beans.iterator().next();
		final CreationalContext<Config> cc = bm.createCreationalContext(bean);

		return (Config) bm.getReference(bean, Config.class, cc);
	}

	static public Config getConfig() {
		try {
			return getConfig(CDI.current().getBeanManager());
		} catch (final IllegalStateException e) {
		}

		return ConfigProvider.getConfig();
	}
}
