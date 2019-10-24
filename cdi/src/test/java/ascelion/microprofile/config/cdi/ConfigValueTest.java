package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class ConfigValueTest {
	@ConfigPrefix("values1")
	static class Values1 {
		@ConfigValue
		boolean booleanValue;
		@ConfigValue
		int intValue;
		@ConfigValue
		long longValue;
		@ConfigValue
		float floatValue;
		@ConfigValue
		double doubleValue;
		@ConfigValue
		String value;
	}

	static class Values2 {
		@ConfigValue("${values2.booleanValue}")
		boolean booleanValue;
		@ConfigValue("values2.intValue")
		int intValue;
		@ConfigValue("values2.longValue")
		long longValue;
		@ConfigValue("values2.floatValue")
		float floatValue;
		@ConfigValue("values2.doubleValue")
		double doubleValue;
		@ConfigValue("values2.value")
		String value;
	}

	@Rule
	public WeldInitiator weld = WeldInitiator
			.from(
					ascelion.microprofile.config.cdi.ConfigExtension.class,
					io.smallrye.config.inject.ConfigExtension.class,
//					org.apache.geronimo.config.cdi.ConfigExtension.class,
					Values1.class)
			.inject(this).build();

	@Inject
	private Values1 values1;
	@Inject
	private Values1 values2;

	@Test
	public void run() {
		assertThat(this.values1.booleanValue, equalTo(true));
		assertThat(this.values1.intValue, equalTo(12));
		assertThat(this.values1.longValue, equalTo(123L));
		assertThat(this.values1.floatValue, equalTo(1234F));
		assertThat(this.values1.doubleValue, equalTo(12345D));

		assertThat(this.values2.booleanValue, equalTo(this.values1.booleanValue));
		assertThat(this.values2.intValue, equalTo(this.values1.intValue));
		assertThat(this.values2.longValue, equalTo(this.values1.longValue));
		assertThat(this.values2.floatValue, equalTo(this.values1.floatValue));
		assertThat(this.values2.doubleValue, equalTo(this.values1.doubleValue));
	}
}
