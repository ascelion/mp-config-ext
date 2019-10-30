package ascelion.microprofile.config.cdi;

import java.util.Properties;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class ComplexPropertiesTest {
	@ConfigPrefix("bean")
	static class Bean {
		@ConfigValue
		Properties properties;
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Bean.class);

	@Inject
	private Bean bean;

	@Test
	public void run() {
		assertThat(this.bean.properties, is(notNullValue()));
		assertThat(this.bean.properties, hasEntry("prop1", "value1"));
		assertThat(this.bean.properties, hasEntry("prop2", "value2"));
	}
}
