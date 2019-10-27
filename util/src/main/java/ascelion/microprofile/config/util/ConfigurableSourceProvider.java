package ascelion.microprofile.config.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigurableSourceProvider extends AbstractConfigSourceProvider {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigurableSourceProvider.class);

	static public String RESOURCE_PROP = "ascelion.microprofile.config.resources";
	static public String DIRECTORY_PROP = "ascelion.microprofile.config.directory";

	@Override
	protected Iterable<ConfigSource> configSources(ClassLoader forClassLoader) {
		final Config config = ConfigInstance.get();
		final File directory = new File(config.getOptionalValue(DIRECTORY_PROP, String.class).orElse("."));

		return config
//				 XXX Payara-5.193 doesn't support String[]
//				.getOptionalValue(RESOURCE_PROP, String[].class)
//				.map(Stream::of)
				.getOptionalValue(RESOURCE_PROP, String.class)
				.map(res -> stream(res.split(",")))
				.orElse(defaultResources())
				.map(String::trim)
				.filter(name -> name.length() > 0)
				.map(name -> name.replace("\\,", ","))
				.flatMap(name -> loadSources(forClassLoader, directory, name))
				.collect(toList());
	}

	protected abstract Stream<String> defaultResources();

	protected final Stream<ConfigSource> loadSources(ClassLoader forClassLoader, File directory, String name) {
		final Collection<ConfigSource> sources = new ArrayList<>();
		final File file = new File(directory, name);

		LOG.debug("Looking for {} ", name);

		if (file.exists()) {
			try {
				LOG.debug("Loading from {}", file);

				sources.add(new PrioritizedConfigSource(loadFrom(file.toURI().toURL())));
			} catch (final IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Exception reading {}", file, e);
				} else {
					LOG.warn("Exception reading {}: {}", file, e.getMessage());
				}
			}
		}

		final Enumeration<URL> resources;

		try {
			resources = forClassLoader.getResources(name);
		} catch (final IOException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception looking for {}", name, e);
			} else {
				LOG.warn("Exception looking for {}", name, e.getMessage());
			}

			return Stream.empty();
		}

		while (resources.hasMoreElements()) {
			final URL url = resources.nextElement();

			try {
				LOG.debug("Loading from {}", url);

				sources.add(loadFrom(url));
			} catch (final IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Exception reading {}", url, e);
				} else {
					LOG.warn("Exception reading {}: {}", url, e.getMessage());
				}
			}
		}

		return sources.stream();
	}

	protected abstract ConfigSource loadFrom(URL resource) throws IOException;
}
