package ascelion.microprofile.config.cdi;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore("not supported yet")
public class NestedBeanWithSetTest {

	@ConfigValue
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@Setter(onParam_ = @ConfigValue)
	@ToString
	static class User {
		String username;
		String password;
		Set<String> roles;
	}

	@ConfigPrefix("nested.bean")
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
		final User user = new User("username", "password", new HashSet<>(asList(roles)));

		assertThat(this.bean.user, equalTo(user));
	}

}
