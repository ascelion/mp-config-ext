package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class NestedValueTest {

	@ConfigPrefix("nested")
	static class Nested {
		@ConfigValue
		int position;

		@ConfigValue("${nested.value}")
		int value;

		@ConfigValue
		int value1;

		@ConfigValue
		int value2;
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Nested.class);

	@Inject
	private Nested nested;

	@Test
	public void run() {
		assertThat(this.nested.position, equalTo(1));
		assertThat(this.nested.value, equalTo(this.nested.value1));
		assertThat(this.nested.value1, equalTo(2));
		assertThat(this.nested.value2, equalTo(3));
	}
}
