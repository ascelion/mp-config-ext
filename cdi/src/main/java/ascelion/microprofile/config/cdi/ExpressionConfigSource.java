package ascelion.microprofile.config.cdi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ascelion.microprofile.config.eval.Expression;
import ascelion.microprofile.config.eval.ExpressionConfig;
import ascelion.microprofile.config.util.AbstractConfigSource;
import ascelion.microprofile.config.util.ConfigInstance;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.eclipse.microprofile.config.Config;

public class ExpressionConfigSource extends AbstractConfigSource {
	private ExpressionConfig expConfig;
	private Expression exp;
	private Map<String, Optional<String>> cache;
	private Map<String, Optional<String>> evals;

	public ExpressionConfigSource() {
		skip(ExpressionConfig.PREFIX_PROP, ExpressionConfig.SUFFIX_PROP, ExpressionConfig.VALUE_PROP, ExpressionConfig.CACHED_PROP);
	}

	@Override
	protected Map<String, String> properties() {
		return remap(expConfig().isCached() ? cache() : readAll());
	}

	@Override
	protected String value(String propertyName) {
		if (expConfig().variableIndex(propertyName) < 0) {
			propertyName = getCachedValue(propertyName, null);

			if (propertyName == null) {
				return null;
			}
		}

		return expConfig().isCached()
				? evals().computeIfAbsent(propertyName, name -> {
					return ofNullable(trimToNull(exp().eval(name)));
				}).orElse(null)
				: trimToNull(exp().eval(propertyName));
	}

	@Override
	public int getOrdinal() {
		return Integer.MAX_VALUE;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	private Map<String, String> remap(Map<String, Optional<String>> map) {
		return unmodifiableMap(map.entrySet().stream()
				.filter(e -> e.getValue().isPresent())
				.collect(toMap(Map.Entry::getKey, e -> e.getValue().get())));
	}

	private String getCachedValue(String propertyName, String defValue) {
		return expConfig().isCached()
				? cache().computeIfAbsent(propertyName, name -> {
					return ofNullable(getStringValue(propertyName, defValue));
				}).orElse(null)
				: getStringValue(propertyName, defValue);
	}

	String getStringValue(String propertyName, String defValue) {
		final Config config = ConfigInstance.get();

		return config
				.getOptionalValue(propertyName, String.class)
				.orElse(trimToNull(defValue));
	}

	private Map<String, Optional<String>> readAll() {
		final Map<String, Optional<String>> props = new HashMap<>();
		final Config config = ConfigInstance.get();

		for (final String name : config.getPropertyNames()) {
			props.put(name, config.getOptionalValue(name, String.class));
		}

		return props;
	}

	private ExpressionConfig expConfig() {
		if (this.expConfig != null) {
			return this.expConfig;
		}

		synchronized (this) {
			if (this.expConfig != null) {
				return this.expConfig;
			}

			this.expConfig = new ExpressionConfig(ConfigInstance.get());

			return this.expConfig;
		}
	}

	private Expression exp() {
		if (this.exp != null) {
			return this.exp;
		}

		synchronized (this) {
			if (this.exp != null) {
				return this.exp;
			}

			this.exp = Expression.builder()
					.withLookup((x, y) -> getCachedValue(x, y))
					.withPrefix(expConfig().getVarPrefix())
					.withSuffix(expConfig().getVarSuffix())
					.withValueSep(expConfig().getValueSep())
					.build();

			return this.exp;
		}

	}

	private Map<String, Optional<String>> cache() {
		if (this.cache != null) {
			return this.cache;
		}

		synchronized (this) {
			if (this.cache != null) {
				return this.cache;
			}

			this.cache = new ConcurrentHashMap<>(readAll());

			return this.cache;
		}
	}

	private Map<String, Optional<String>> evals() {
		if (this.evals != null) {
			return this.evals;
		}

		synchronized (this) {
			if (this.evals != null) {
				return this.evals;
			}

			return this.evals = new ConcurrentHashMap<>();
		}
	}

}
