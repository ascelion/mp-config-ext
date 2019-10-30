package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class NestedBeanTest {

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
		User user;
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Bean.class);

	@Inject
	private Bean bean;

	@Test
	public void run() {
		final String[] roles = { "role1", "role2" };
		final User user = new User("username", "password", roles);

		assertThat(this.bean.user, equalTo(user));
	}

}
