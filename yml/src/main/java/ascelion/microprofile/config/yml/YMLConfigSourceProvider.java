package ascelion.microprofile.config.yml;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import ascelion.microprofile.config.util.ConfigurableSourceProvider;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class YMLConfigSourceProvider extends ConfigurableSourceProvider {
	@Override
	protected Stream<String> defaultResources() {
		return Stream.of("microprofile-config.yml");
	}

	@Override
	protected ConfigSource loadFrom(URL url) throws IOException {
		return new YMLConfigSource(url);
	}
}
