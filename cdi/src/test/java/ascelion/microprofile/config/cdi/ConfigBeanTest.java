//package ascelion.microprofile.config.cdi;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//import java.util.TreeSet;
//
//import javax.inject.Inject;
//
//import ascelion.microprofile.config.ConfigPrefix;
//import ascelion.microprofile.config.ConfigValue;
//
//import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.hasEntry;
//import static org.hamcrest.Matchers.hasItem;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.notNullValue;
//import static org.junit.Assert.assertThat;
//
//import org.jboss.weld.junit4.WeldInitiator;
//import org.junit.Ignore;
//import org.junit.Rule;
//import org.junit.Test;
//
//@Ignore
//public class ConfigBeanTest {
//	@ConfigPrefix("bean")
//	static class Bean {
//		@ConfigValue
//		User user;
//
//		@ConfigValue
//		Properties properties;
//
//		@ConfigValue("values")
//		Map<String, Integer> valuesI;
//		@ConfigValue(value = "values", hint = LinkedHashMap.class)
//		Map<String, String> valuesS;
//
//		@ConfigValue("users")
//		Collection<User> usersC;
//		@ConfigValue("users")
//		List<User> usersL;
//		@ConfigValue("users")
//		Set<User> usersS;
//
//		@ConfigValue(value = "users", hint = LinkedList.class)
//		List<User> usersLL;
//		@ConfigValue(value = "users", hint = TreeSet.class)
//		Set<User> usersTS;
//
//		@ConfigValue("users")
//		Map<String, User> usersM;
//	}
//
//	@Rule
//	public WeldInitiator weld = createWeldRule(this, Bean.class);
//
//	@Inject
//	private Bean bean;
//
//	@Test
//	public void nested() {
//		assertThat(this.bean.user, is(notNullValue()));
//		assertThat(this.bean.user.username, equalTo("username"));
//		assertThat(this.bean.user.password, equalTo("password"));
//	}
//
//	@Test
//	public void properties() {
//		assertThat(this.bean.properties, is(notNullValue()));
//		assertThat(this.bean.properties, hasEntry("prop1", "value1"));
//		assertThat(this.bean.properties, hasEntry("prop2", "value2"));
//	}
//
//	@Test
//	public void map() {
//		assertThat(this.bean.valuesI, is(notNullValue()));
//		assertThat(this.bean.valuesS, is(instanceOf(HashMap.class)));
//		assertThat(this.bean.valuesI, hasEntry("prop1", 1));
//		assertThat(this.bean.valuesI, hasEntry("prop2", 2));
//	}
//
//	@Test
//	public void linkedHashMap() {
//		assertThat(this.bean.valuesS, is(notNullValue()));
//		assertThat(this.bean.valuesS, is(instanceOf(LinkedHashMap.class)));
//		assertThat(this.bean.valuesS, hasEntry("prop1", "1"));
//		assertThat(this.bean.valuesS, hasEntry("prop2", "2"));
//	}
//
//	@Test
//	public void list() {
//		assertThat(this.bean.usersC, is(notNullValue()));
//		assertThat(this.bean.usersC, is(instanceOf(ArrayList.class)));
//		assertThat(this.bean.usersC, hasItem(new User("prop1", "1", new String[] { "role1" })));
//		assertThat(this.bean.usersC, hasItem(new User("prop2", "2", new String[] { "role2" })));
//	}
//}
