package ascelion.microprofile.config.util;

import static java.util.Collections.emptyList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public abstract class AbstractConfigSourceProvider implements ConfigSourceProvider {

	static private final ThreadLocal<Boolean> RECURSIVE = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		};
	};

	private Config config;

	@Override
	public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
		if (RECURSIVE.get()) {
			return emptyList();
		}

		RECURSIVE.set(true);

		try {
			return configSources(forClassLoader);
		} finally {
			RECURSIVE.remove();
		}
	}

	protected final Config config() {
		if (this.config != null) {
			return this.config;
		}

		synchronized (this) {
			if (this.config != null) {
				return this.config;
			}

			this.config = ConfigUtil.getConfig();

			return this.config;
		}
	}

	protected abstract Iterable<ConfigSource> configSources(ClassLoader forClassLoader);
}
