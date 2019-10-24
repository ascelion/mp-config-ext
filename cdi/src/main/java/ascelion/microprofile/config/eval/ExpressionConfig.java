package ascelion.microprofile.config.eval;

import lombok.Getter;
import org.eclipse.microprofile.config.Config;

@Getter
public final class ExpressionConfig {
	static public final String PREFIX_PROP = "ascelion.microprofile.config.var.prefix";
	static public final String SUFFIX_PROP = "ascelion.microprofile.config.var.suffix";
	static public final String VALUE_PROP = "ascelion.microprofile.config.var.default";
	static public final String CACHED_PROP = "ascelion.microprofile.config.var.cached";

	static public final String PREFIX_DEF = "${";
	static public final String SUFFIX_DEF = "}";
	static public final String VALUE_DEF = ":-";
	static public final boolean CACHED_DEF = false;

	private final String varPrefix;
	private final String varSuffix;
	private final String valueSep;
	private final boolean cached;

	public ExpressionConfig(Config config) {
		this.varPrefix = config.getOptionalValue(ExpressionConfig.PREFIX_PROP, String.class).orElse(ExpressionConfig.PREFIX_DEF);
		this.varSuffix = config.getOptionalValue(ExpressionConfig.SUFFIX_PROP, String.class).orElse(ExpressionConfig.SUFFIX_DEF);
		this.valueSep = config.getOptionalValue(ExpressionConfig.VALUE_PROP, String.class).orElse(ExpressionConfig.VALUE_DEF);
		this.cached = config.getOptionalValue(ExpressionConfig.CACHED_PROP, Boolean.class).orElse(ExpressionConfig.CACHED_DEF);
	}

	public int variableIndex(String text) {
		return text.indexOf(this.varPrefix);
	}
}
