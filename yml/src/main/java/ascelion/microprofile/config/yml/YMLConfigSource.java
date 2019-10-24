package ascelion.microprofile.config.yml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.yaml.snakeyaml.Yaml;

@ToString(of = "name")
class YMLConfigSource implements ConfigSource {
	static private String path(String... names) {
		return Stream.of(names).filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
	}

	static private String encode(Stream<?> stream) {
		return stream
				.map(Object::toString)
				.map(String::trim)
				.filter(s -> s.length() > 0)
				.map(s -> s.replace(",", "\\,"))
				.collect(Collectors.joining(","));
	}

	private final String name;
	private final Map<String, String> properties = new TreeMap<>();
	@Getter
	private final int ordinal;

	YMLConfigSource(URL url) throws IOException {
		this.name = url.toExternalForm();

		final Yaml yaml = new Yaml();

		try (InputStream is = url.openStream()) {
			yaml.loadAll(is)
					.forEach(o -> {
						add("", o);
					});
		}

		int ord;

		try {
			ord = Integer.parseInt(this.properties.getOrDefault(CONFIG_ORDINAL, ""));
		} catch (final Exception e) {
			ord = DEFAULT_ORDINAL;
		}

		this.ordinal = ord;
	}

	@Override
	public Map<String, String> getProperties() {
		return unmodifiableMap(this.properties);
	}

	@Override
	public String getValue(String propertyName) {
		return this.properties.get(propertyName);
	}

	@Override
	public String getName() {
		return this.name;
	}

	private void add(String prefix, Object value) {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> ms = (Map<String, Object>) value;

			ms.forEach((k, s) -> {
				add(path(prefix, k), s);
			});

			return;
		}
		if (value instanceof Collection) {
			add(prefix, encode(((Collection<?>) value).stream()));

			return;
		}
		if (value instanceof Object[]) {
			add(prefix, encode(stream((Object[]) value)));

			return;
		}

		this.properties.put(prefix, Objects.toString(value, null));
	}
}
