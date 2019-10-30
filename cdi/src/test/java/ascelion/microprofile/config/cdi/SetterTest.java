package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class SetterTest {
	static class Bean {
		boolean configureCalled;
		boolean definedCalled;
		boolean undefinedCalled;

		public void configure(@ConfigValue String configured) {
			this.configureCalled = true;
		}

		public void setDefined(@ConfigValue(required = false) String defined) {
			this.definedCalled = true;
		}

		public void setUndefined(@ConfigValue(required = false) String undefined) {
			this.undefinedCalled = true;
		}

	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Bean.class);

	@Inject
	private Bean bean;

	@Test
	public void run() {
		assertThat(this.bean.configureCalled, is(true));
		assertThat(this.bean.definedCalled, is(true));
		assertThat(this.bean.undefinedCalled, is(false));
	}
}
