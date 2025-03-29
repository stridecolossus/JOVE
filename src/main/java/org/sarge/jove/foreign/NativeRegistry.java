package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.ValueLayout;
import java.util.*;
import java.util.logging.Logger;

import org.sarge.jove.foreign.NativeTransformer.Factory;

/**
 * The <i>native registry</i> maps a domain type to the corresponding native transformer.
 * @author Sarge
 */
public class NativeRegistry {
	private static final Logger LOG = Logger.getLogger(NativeRegistry.class.getName());

	private final Map<Class<?>, Factory<?>> registry = new HashMap<>();

	/**
	 * Looks up the native transformer for the given Java type.
	 * TODO - arrays, wrapper for reference types, derived
	 * @param type Java type
	 * @return Native transformer
	 * @throws IllegalArgumentException if {@link #type} is unsupported
	 */
	public NativeTransformer<?> transformer(Class<?> type) {
		final Factory<?> factory = registry.get(type);

		if(factory == null) {
			if(type.isArray()) {
				return array(type);
			}
			else {
				return derived(type);
			}
		}
		else {
			return create(factory, type);
		}
	}

	/**
	 * Finds the transformer that supports a given domain subtype.
	 * Registers the subtype mapping as a side effect.
	 * @param type Subtype
	 * @return Subtype transformer
	 */
	private NativeTransformer<?> derived(Class<?> type) {
		final Factory<?> derived = find(type).orElseThrow(() -> new IllegalArgumentException("Unsupported native type: " + type.getSimpleName()));
		LOG.info("Found subtype transformer: type=%s transformer=%s".formatted(type, derived));
		registry.put(type, derived);
		return create(derived, type);
	}

	// TODO
	private ArrayNativeTransformer array(Class<?> type) {
		final Class<?> component = type.getComponentType();
    	final NativeTransformer<?> transformer = transformer(component);
    	final var array = new ArrayNativeTransformer(transformer);
		LOG.info("Found array transformer: component=%s transformer=%s".formatted(component, transformer));
		// TODO - registry.put(type, _ -> array);
    	return array;
	}

	/**
	 * Instantiates a transformer instance which is also wrapped with a null-safe adapter for reference types.
	 * @param factory		Transformer factory
	 * @param type			Domain type
	 * @return Transformer
	 */
	private static NativeTransformer<?> create(Factory<?> factory, Class<?> type) {
		@SuppressWarnings({"unchecked", "rawtypes"})
		final NativeTransformer<?> transformer = factory.create((Class) type);

		if(ValueLayout.ADDRESS.equals(transformer.layout())) {
			return new NativeTransformerAdapter<>(transformer);
		}
		else {
			return transformer;
		}
	}

	/**
	 * Finds a factory that supports the given sub-type.
	 * Note that a matched sub-type factory is registered as a side effect.
	 * @param type Domain sub-type
	 * @return Native transformer factory
	 */
	private Optional<Factory<?>> find(Class<?> type) {
		return registry
				.keySet()
				.stream()
				.filter(e -> e.isAssignableFrom(type))
				.findAny()
				.map(registry::get);
	}

	/**
	 * Registers a native transformer factory.
	 * @param <T> Domain type
	 * @param type			Java type
	 * @param factory		Transformer factory
	 */
	public <T> void add(Class<T> type, Factory<T> factory) {
		requireNonNull(type);
		requireNonNull(factory);
		registry.put(type, factory);
		LOG.info("Register transformer factory: type=%s transformer=%s".formatted(type, factory));
	}

	/**
	 * Registers a native transformer.
	 * @param <T> Domain type
	 * @param type				Java type
	 * @param transformer		Native transformer
	 * @see #add(Class, Factory)
	 */
	public <T> void add(Class<T> type, NativeTransformer<T> transformer) {
		requireNonNull(transformer);
		add(type, _ -> transformer);
	}

	/**
	 * Creates a native registry including transformers for the built-in primitive types.
	 * @return Default native registry
	 */
	public static NativeRegistry create() {
		final var registry = new NativeRegistry();

		final var primitives = Map.of(
				boolean.class,	JAVA_BOOLEAN,
				byte.class,		JAVA_BYTE,
				char.class,		JAVA_CHAR,
				short.class,	JAVA_SHORT,
				int.class,		JAVA_INT,
				long.class,		JAVA_LONG,
				float.class,	JAVA_FLOAT,
				double.class,	JAVA_DOUBLE
		);

		for(var entry : primitives.entrySet()) {
			registry.add(entry.getKey(), new IdentityNativeTransformer<>(entry.getValue()));
		}

		return registry;
	}

	@Override
	public String toString() {
		return registry.toString();
	}
}
