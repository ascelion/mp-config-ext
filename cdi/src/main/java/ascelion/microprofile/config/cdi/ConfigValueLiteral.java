package ascelion.microprofile.config.cdi;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.microprofile.config.ConfigValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue {

	private final String value;
	private final boolean required;

	@Override
	public String value() {
		return this.value;
	}

	@Override
	public boolean required() {
		return this.required;
	}
}
