package ascelion.microprofile.config.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface ConfigSet {
	class Literal extends AnnotationLiteral<ConfigSet> implements ConfigSet {
	};

	ConfigSet INSTANCE = new Literal();
}
