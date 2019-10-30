package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class NestedBeansTest {

	@ConfigValue
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@Setter(onParam_ = @ConfigValue)
	static class User {
		String username;
		String password;
		String[] roles;
	}

	@ConfigPrefix("bean")
	static class Bean {
		@ConfigValue
		User[] users;
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Bean.class);

	@Inject
	private Bean bean;

	@Test
	public void run() {
		assertThat(this.bean.users, is(notNullValue()));
		assertThat(asList(this.bean.users), hasSize(2));
	}
}
