
package ascelion.microprofile.config.eval;

import java.util.function.BinaryOperator;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class ExpressionTest {

	@Test
	public void run00() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("${a}");

		assertThat(val, is("<a>"));
	}

	@Test
	public void run01_1() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("--${a-${b}-c}--");

		assertThat(val, is("--<a-<b>-c>--"));
	}

	@Test
	public void run01_2() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("{a-${b-${c-${d}-e}-f}-g}");

		assertThat(val, is("{a-<b-<c-<d>-e>-f>-g}"));
	}

	@Test
	public void run02() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("x-${a-null:-b}-y");

		assertThat(val, is("x-b-y"));
	}

	@Test
	public void run03() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("x-${a-null:-${b-null:-c}}-y");

		assertThat(val, is("x-c-y"));
	}

	@Test
	public void run04() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("{a-${b-${c-null:-x-${y}-z}-d}-e}");

		assertThat(val, is("{a-<b-x-<y>-z-d>-e}"));
	}

	@Test
	public void run05() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("$a:b");

		assertThat(val, is("$a:b"));
	}

	@Test
	@Ignore
	public void run06_1() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("${a\\:b}");

		assertThat(val, is("<a:b>"));
	}

	@Test
	@Ignore
	public void run06_2() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("${a\\\\:b}");

		assertThat(val, is("<a\\>"));
	}

	@Test
	public void run07() {
		final Expression exp = Expression.builder().withLookup(this::mockEval).build();
		final String val = exp.eval("${a-null:-b:-c}");

		assertThat(val, is("b:-c"));
	}

	@Test(expected = IllegalStateException.class)
	public void runLoop() {
		final int[] count = { 0 };
		final BinaryOperator<String> fun = (x, y) -> {
			return format("${X%02d}", ++count[0] % 5);
		};

		final Expression exp = Expression.builder()
				.withLookup(fun)
				.build();

		exp.eval("${X00}");
	}

	private String mockEval(String x, String y) {
		if (isBlank(x)) {
			return y;
		}

		return x.startsWith("null-") || x.endsWith("-null") ? y : "<" + x + ">";
	}
}
