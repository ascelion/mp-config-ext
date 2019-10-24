package ascelion.microprofile.config.cdi;

import javax.inject.Inject;

import ascelion.microprofile.config.ConfigValue;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

public class ConfigInjectTest {
	static class Bean1 {
		@ConfigValue("user.home")
		String userHome;

		String userDir;
		String javaVersion;

		String classpath;

		boolean undefinedCalled;

		void inject(@ConfigValue("user.dir") String userDir, @ConfigValue("java.version") String javaVersion) {
			this.userDir = userDir;
			this.javaVersion = javaVersion;
		}

		void setClasspath(@ConfigValue("java.class.path") String classpath) {
			this.classpath = classpath;
		}

		void setUndefined(@ConfigValue(value = "undefined", required = false) String undefined) {
			this.undefinedCalled = true;
		}
	}

	static class Bean2 {
		@Inject
		@ConfigProperty(name = "user.home")
		String userHome;

		String userDir;
		String javaVersion;

		String classpath;

		@Inject
		void inject(@ConfigProperty(name = "user.dir") String userDir, @ConfigProperty(name = "java.version") String javaVersion) {
			this.userDir = userDir;
			this.javaVersion = javaVersion;
		}

		@Inject
		void setClasspath(@ConfigProperty(name = "java.class.path") String classpath) {
			this.classpath = classpath;
		}
	}

	@Rule
	public WeldInitiator weld = WeldInitiator
			.from(
					ascelion.microprofile.config.cdi.ConfigExtension.class,
					io.smallrye.config.inject.ConfigExtension.class,
//					org.apache.geronimo.config.cdi.ConfigExtension.class,
					Bean1.class, Bean2.class)
			.inject(this)
			.build();

	@Inject
	private Bean1 bean1;
	@Inject
	private Bean2 bean2;

	@Test
	public void run() {
		assertThat(this.bean1.userHome, equalTo(this.bean2.userHome));
		assertThat(this.bean1.userDir, equalTo(this.bean2.userDir));
		assertThat(this.bean1.javaVersion, equalTo(this.bean2.javaVersion));
		assertThat(this.bean1.classpath, equalTo(this.bean2.classpath));

		assertThat(this.bean1.undefinedCalled, is(false));
	}

}
