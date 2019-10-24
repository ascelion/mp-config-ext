package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

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

		@ConfigValue
		int value1;

		@ConfigValue
		int value2;

		@ConfigValue("${nested.value}")
		int value;
	}

	@Rule
	public WeldInitiator weld = WeldInitiator
			.from(
					ascelion.microprofile.config.cdi.ConfigExtension.class,
					io.smallrye.config.inject.ConfigExtension.class,
//					org.apache.geronimo.config.cdi.ConfigExtension.class,
					Nested.class)
			.inject(this).build();

	@Inject
	private Nested nested;

	@Test
	public void run() {
		assertThat(this.nested.position, equalTo(1));
		assertThat(this.nested.value1, equalTo(2));
		assertThat(this.nested.value2, equalTo(3));
		assertThat(this.nested.value, equalTo(this.nested.value1));
	}
}
