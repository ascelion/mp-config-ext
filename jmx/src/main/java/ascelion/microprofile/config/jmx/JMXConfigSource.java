package ascelion.microprofile.config.jmx;

import java.util.Map;

import ascelion.microprofile.config.util.AbstractConfigSource;

public class JMXConfigSource extends AbstractConfigSource {
	static final String DOMAIN_PROP = "ascelion.microprofile.config.jmx.domain";

	public JMXConfigSource() {
		skip(DOMAIN_PROP);
	}

	@Override
	public String getName() {
		return "JMX";
	}

	@Override
	protected String value(String propertyName) {
		return null;
	}

	@Override
	protected Map<String, String> properties() {
		return null;
	}
}
