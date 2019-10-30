package ascelion.microprofile.config.cdi;

import java.util.Map;

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

public class ComplexMapIntTest {
	@ConfigPrefix("bean")
	static class Bean {
		@ConfigValue
		Map<String, Integer> values;
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Bean.class);

	@Inject
	private Bean bean;

	@Test
	public void map() {
		assertThat(this.bean.values, is(notNullValue()));
		assertThat(this.bean.values, hasEntry("prop1", 1));
		assertThat(this.bean.values, hasEntry("prop2", 2));
	}
}
