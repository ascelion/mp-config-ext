package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigValue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class SetterTest {
	static public class Bean {
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
	public WeldInitiator weld = WeldInitiator
			.from(
					ascelion.microprofile.config.cdi.ConfigExtension.class,
					io.smallrye.config.inject.ConfigExtension.class,
//					org.apache.geronimo.config.cdi.ConfigExtension.class,
					Bean.class)
			.inject(this)
			.build();

	@Inject
	private Bean bean;

	@Test
	public void run() {
		assertThat(this.bean.configureCalled, is(true));
		assertThat(this.bean.definedCalled, is(true));
		assertThat(this.bean.undefinedCalled, is(false));
	}
}
