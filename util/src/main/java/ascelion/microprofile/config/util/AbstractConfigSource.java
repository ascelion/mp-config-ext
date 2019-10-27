package ascelion.microprofile.config.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class AbstractConfigSource implements ConfigSource {

	static private final ThreadLocal<Boolean> RECURSIVE = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		};
	};

	private final Set<String> skipped = new HashSet<>();

	protected final void skip(String name, String... names) {
		this.skipped.add(name);
		this.skipped.addAll(asList(names));
	}

	@Override
	public final Set<String> getPropertyNames() {
		if (RECURSIVE.get()) {
			return emptySet();
		}

		RECURSIVE.set(true);

		try {
			final Set<String> names = new HashSet<>(propertyNames());

			names.removeAll(this.skipped);

			return names;
		} finally {
			RECURSIVE.remove();
		}
	}

	@Override
	public final Map<String, String> getProperties() {
		if (RECURSIVE.get()) {
			return emptyMap();
		}

		RECURSIVE.set(true);

		try {
			final Map<String, String> props = new HashMap<>(properties());

			this.skipped.forEach(props::remove);

			return props;
		} finally {
			RECURSIVE.remove();
		}
	}

	@Override
	public final String getValue(String propertyName) {
		if (RECURSIVE.get()) {
			return null;
		}

		RECURSIVE.set(true);

		try {
			return this.skipped.contains(propertyName) ? null : value(propertyName);
		} finally {
			RECURSIVE.remove();
		}
	}

	protected abstract String value(String propertyName);

	protected Set<String> propertyNames() {
		return properties().keySet();
	}

	protected abstract Map<String, String> properties();
}
