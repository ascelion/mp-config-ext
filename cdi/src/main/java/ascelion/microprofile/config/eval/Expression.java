package ascelion.microprofile.config.eval;

import java.util.function.BinaryOperator;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Expression {
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static public class Builder {
		private BinaryOperator<String> lookup = (x, y) -> y;

		private char[] varPrefix;
		private char[] valueSep;
		private char[] varSuffix;

		public Expression build() {
			return new Expression(this.lookup, this.varPrefix, this.valueSep, this.varSuffix);
		}

		public Builder withLookup(BinaryOperator<String> lookup) {
			this.lookup = lookup;

			return this;
		}

		public Builder withPrefix(String expPrefix) {
			this.varPrefix = expPrefix.toCharArray();

			return this;
		}

		public Builder withValueSep(String expDefault) {
			this.valueSep = expDefault.toCharArray();

			return this;
		}

		public Builder withSuffix(String expSuffix) {
			this.varSuffix = expSuffix.toCharArray();

			return this;
		}
	}

	public static Builder builder() {
		return new Builder()
				.withPrefix(ExpressionConfig.PREFIX_DEF)
				.withValueSep(ExpressionConfig.VALUE_DEF)
				.withSuffix(ExpressionConfig.SUFFIX_DEF);
	}

	@NonNull
	final BinaryOperator<String> lookup;
	@NonNull
	final char[] varPrefix;
	@NonNull
	final char[] valueSep;
	@NonNull
	final char[] varSuffix;

	public String eval(String expression) {
		return new Replacer(this)
				.replace(new Buffer(expression))
				.toString();
	}
}
