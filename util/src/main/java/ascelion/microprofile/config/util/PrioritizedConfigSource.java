package ascelion.microprofile.config.util;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

class PrioritizedConfigSource implements ConfigSource {

	private final ConfigSource delegate;
	private final int ordinal;

	PrioritizedConfigSource(ConfigSource delegate) {
		this.delegate = delegate;
		this.ordinal = delegate.getOrdinal() + 1;
	}

	@Override
	public Map<String, String> getProperties() {
		return this.delegate.getProperties();
	}

	@Override
	public Set<String> getPropertyNames() {
		return this.delegate.getPropertyNames();
	}

	@Override
	public String getValue(String propertyName) {
		return this.delegate.getValue(propertyName);
	}

	@Override
	public String getName() {
		return this.delegate.getName();
	}

	@Override
	public int getOrdinal() {
		return this.ordinal;
	}
}
