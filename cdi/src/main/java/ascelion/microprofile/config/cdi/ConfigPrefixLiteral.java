package ascelion.microprofile.config.cdi;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.microprofile.config.ConfigPrefix;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConfigPrefixLiteral extends AnnotationLiteral<ConfigPrefix> implements ConfigPrefix {
	private final String value;

	@Override
	public String value() {
		return this.value;
	}
}
