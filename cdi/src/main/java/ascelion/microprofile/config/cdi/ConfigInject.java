package ascelion.microprofile.config.cdi;

import java.beans.Introspector;
import java.lang.reflect.Executable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;

import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.cdi.metadata.AnnotatedTypeModifier.Modifier;
import ascelion.microprofile.config.ConfigPrefix;
import ascelion.microprofile.config.ConfigValue;
import ascelion.microprofile.config.eval.ExpressionConfig;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConfigInject<T> {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigInject.class);

	private final ExpressionConfig subConfig;
	private final AnnotatedTypeModifier<T> mod;
	private final String prefix;
	private final Set<ConfigValue> values = new HashSet<>();

	private final String typeName;

	ConfigInject(ExpressionConfig subConfig, AnnotatedType<T> type) {
		this.subConfig = subConfig;
		this.typeName = type.getJavaClass().getCanonicalName();
		this.mod = AnnotatedTypeModifier.create(type);
		this.prefix = ofNullable(type.getAnnotation(ConfigPrefix.class))
				.map(ConfigPrefix::value)
				.orElse(null);
	}

	Set<ConfigValue> values() {
		if (this.values.isEmpty()) {
			processCallables(this.mod.getCallables());
			processFields(this.mod.getFields());
		}

		return this.values;
	}

	AnnotatedType<T> type() {
		return this.mod.get();
	}

	@SuppressWarnings("unchecked")
	private void processCallables(Set<AnnotatedCallable<? super T>> callables) {
		callables.stream()
				.filter(callable -> !callable.isAnnotationPresent(Produces.class))
				.filter(callable -> callable.getParameters().size() > 0)
				.filter(callable -> callable.getParameters().stream().anyMatch(p -> p.isAnnotationPresent(ConfigValue.class)))
				.forEach(callable -> processCallable((AnnotatedCallable<T>) callable));
	}

	private void processCallable(AnnotatedCallable<T> callable) {
		final Executable executable = (Executable) callable.getJavaMember();
		final List<AnnotatedParameter<T>> parameters = callable.getParameters();

		if (parameters.size() == 1) {
			final AnnotatedParameter<? super T> parameter = parameters.get(0);
			final ConfigValue cval = parameter.getAnnotation(ConfigValue.class);
			final Modifier<T> mod = this.mod.executableParam(executable, 0);
			String name = executable.getName();

			if (name.startsWith("set")) {
				if (cval.required()) {
					this.mod.executable(executable)
							.add(InjectLiteral.INSTANCE);
				} else {
					this.mod.executable(executable)
							.remove(Inject.class)
							.add(Setter.INSTANCE);

					LOG.debug("Removing injection from setter in {}", executable);
				}

				name = Introspector.decapitalize(name.substring(3));
			} else {
				this.mod.executable(executable)
						.add(InjectLiteral.INSTANCE);
			}

			updateAnnotation(cval, mod, name);
		} else {
			for (int k = 0; k < parameters.size(); k++) {
				final AnnotatedParameter<? super T> parameter = parameters.get(k);

				if (parameter.isAnnotationPresent(ConfigValue.class)) {
					final ConfigValue cval = parameter.getAnnotation(ConfigValue.class);
					final Modifier<T> mod = this.mod.executableParam(executable, k);

					updateAnnotation(cval, mod, "");
				}
			}

			this.mod.executable(executable)
					.add(InjectLiteral.INSTANCE);
		}
	}

	private void processFields(Set<AnnotatedField<? super T>> fields) {
		fields.stream()
				.filter(field -> !field.isAnnotationPresent(Produces.class))
				.filter(field -> field.isAnnotationPresent(ConfigValue.class))
				.forEach(field -> processField(field));
	}

	private void processField(AnnotatedField<? super T> annotated) {
		final Modifier<T> fmod = this.mod.field(annotated);

		fmod.add(InjectLiteral.INSTANCE);

		final ConfigValue cval = annotated.getAnnotation(ConfigValue.class);

		updateAnnotation(cval, fmod, annotated.getJavaMember().getName());
	}

	private ConfigValue updateAnnotation(ConfigValue cval, Modifier<T> amod, String name) {
		final String prop = cval.value();
		final int varIx = this.subConfig.variableIndex(prop);

		amod.remove(ConfigValue.class);

		final StringBuilder expr = new StringBuilder();

		if (prop.isEmpty()) {
			if (name.isEmpty()) {
				throw new DeploymentException(format("Configuration name is required for %s", amod.and().get()));
			}
			if (varIx < 0) {
				if (this.prefix == null) {
					expr.append(this.typeName);
				} else {
					expr.append(this.prefix);
				}

				expr.append(".");
			}

			expr.append(name);
		} else {
			if (varIx < 0 && this.prefix != null) {
				expr.append(this.prefix);
				expr.append(".");
			}

			expr.append(prop);
		}

		if (varIx < 0) {
			expr.insert(0, this.subConfig.getVarPrefix());
			expr.append(this.subConfig.getVarSuffix());
		}

		cval = new ConfigValueLiteral(expr.toString(), cval.required());

		this.values.add(cval);

		amod.add(cval);

		LOG.debug("Set {} to {}", expr, amod.and().get());

		return cval;
	}
}
