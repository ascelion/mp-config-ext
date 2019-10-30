package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;

import static ascelion.microprofile.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class ConfigPrefixTest {
	@ConfigPrefix("database")
	static class Database {
		@ConfigValue
		String username;
		@ConfigValue
		String password;

		String serverName;

		int port;

		@Inject
		Database(@ConfigValue("port") int port) {
			this.port = port;
		}

		public void setServerName(@ConfigValue String serverName) {
			this.serverName = serverName;
		}
	}

	@Rule
	public WeldInitiator weld = createWeldRule(this, Database.class);

	@Inject
	private Database db;

	@Test
	public void run() {
		assertThat(this.db.username, equalTo("username"));
		assertThat(this.db.password, equalTo("password"));
		assertThat(this.db.serverName, equalTo("localhost"));
	}
}
