package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class GenericConfigTest {

	static class Generic<T> {
		@ConfigValue("generic")
		T value;
	}

	static class GenericInteger extends Generic<Integer> {
	}

	static class GenericLong extends Generic<Long> {
	}

	static class GenericString extends Generic<String> {
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, GenericInteger.class, GenericLong.class, GenericString.class);

	@Inject
	private GenericInteger gInteger;
	@Inject
	private GenericLong gLong;
	@Inject
	private GenericString gString;

	@Test
	public void run() {
		assertThat(this.gInteger.value, equalTo(314));
		assertThat(this.gLong.value, equalTo(314L));
		assertThat(this.gString.value, equalTo("314"));
	}

}
