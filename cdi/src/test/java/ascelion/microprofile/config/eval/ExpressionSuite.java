
package ascelion.microprofile.config.eval;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.apache.commons.text.StringSubstitutor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.yaml.snakeyaml.Yaml;

@RunWith(Parameterized.class)
public class ExpressionSuite {

	@Parameterized.Parameters(name = "{0}")
	static public Object data() throws IOException {
		final ClassLoader cld = currentThread().getContextClassLoader();
		final List<Object[]> data = new ArrayList<>();

		try (InputStream is = cld.getResourceAsStream(ExpressionSuite.class.getSimpleName() + ".yml")) {
			final Yaml yml = new Yaml();
			final List<Map<String, Object>> all = (List<Map<String, Object>>) yml.load(is);

			for (final Map<String, Object> td : all) {
				final String tn = ((String) td.get("expression"));

				data.add(new Object[] {
						format("%02d-%s", data.size(), tn),
						td,
				});
			}
		}

		return data;
	}

	@SuppressWarnings("unchecked")
	static Class<? extends Exception> getException(Map<String, Object> td, String name) throws ClassNotFoundException {
		final String exception = (String) td.get(name);

		if (exception != null) {
			return (Class<? extends Exception>) Class.forName(exception);
		}

		return null;
	}

	@Rule
	public final ExpectedException exRule = ExpectedException.none();

	private final String expression;
	private final String expected;
	private final String expectedCT;
	private final Map<String, String> properties;
	private final Class<? extends Exception> exception;
	private final Class<? extends Exception> exceptionCT;

	@SuppressWarnings("unchecked")
	public ExpressionSuite(String unused, Map<String, Object> td) throws ClassNotFoundException {
		this.expression = (String) td.get("expression");
		this.properties = (Map<String, String>) td.get("properties");
		this.expected = (String) td.get("expected");
		this.expectedCT = (String) td.get("expectedCT");
		this.exception = getException(td, "exception");
		this.exceptionCT = getException(td, "exceptionCT");
	}

	@Test
	public void commonsText() throws Throwable {
		final StringSubstitutor sub = new StringSubstitutor();

		sub.setEscapeChar('^');
		sub.setVariablePrefix("@{");
		sub.setPreserveEscapes(false);
		sub.setEnableSubstitutionInVariables(true);
		sub.setEnableUndefinedVariableException(true);
		sub.setVariableResolver(this::lookup);

		if (this.exceptionCT != null) {
			this.exRule.expect(this.exceptionCT);
		} else if (this.exception != null) {
			this.exRule.expect(this.exception);
		}

		final String text = sub.replace(this.expression);

		if (this.expectedCT != null) {
			assertThat(text, is(this.expectedCT));
		} else {
			assertThat(text, is(this.expected));
		}
	}

	@Test
	public void expression() throws Throwable {
		if (this.exception != null) {
			this.exRule.expect(this.exception);
		}

		final Expression exp = Expression
				.builder()
				.withLookup(this::lookup)
				.withPrefix("@{")
				.build();

		final String text = exp.eval(this.expression);

		assertThat(text, is(this.expected));
	}

	private String lookup(String key) {
		if (this.properties == null || !this.properties.containsKey(key)) {
			return null;
		}

		return this.properties.get(key);
	}

	private String lookup(String key, String def) {
		if (this.properties == null || !this.properties.containsKey(key)) {
			if (def != null) {
				return def;
			}

			throw new IllegalArgumentException("Cannot find property " + key);
		}

		return this.properties.get(key);
	}

}
