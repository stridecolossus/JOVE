package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.util.*;

/**
 * A <i>registry</i> maps built-in and domain types to the corresponding native transformer.
 * @author Sarge
 */
public class Registry {
	/**
	 * A <i>transformer factory</i> generates a transformer for a given subclass.
	 * @param <T> Type
	 */
	@FunctionalInterface
	public interface Factory<T> {
		/**
		 * Creates a transformer for the given type.
		 * @param type Type
		 * @return Transformer
		 */
		Transformer<T, ?> transformer(Class<? extends T> type);
	}

	@SuppressWarnings("rawtypes")
	private final Map<Class<?>, Transformer> registry = new HashMap<>();
	private final Map<Class<?>, Factory<?>> factories = new HashMap<>();

	/**
	 * Finds the registered transformer for the given type.
	 * @param type Type
	 * @return Transformer
	 */
	@SuppressWarnings("rawtypes")
	public Optional<Transformer> transformer(Class<?> type) {
		return Optional
				.ofNullable(registry.get(type))
				.or(() -> find(type));
	}

	/**
	 * Finds and registers the transformer for the given type.
	 * @param type Type
	 * @return Transformer
	 */
	@SuppressWarnings("rawtypes")
	private Optional<Transformer> find(Class<?> type) {
		if(type.isArray()) {
			final Class<?> component = type.getComponentType();
    		final var array = transformer(component).map(Transformer::array);
    		register(type, array);
    		return array;
    	}
		else {
			final Optional<Transformer> transformer = find(type, registry).or(() -> factory(type));
    		register(type, transformer);
			return transformer;
		}
	}

	/**
	 * Finds the value in the given map with a supertype of the given type.
	 * @param <T> Value type
	 * @param type		Type
	 * @param map		Map indexed by type
	 * @return Matched value
	 */
	private static <T> Optional<T> find(Class<?> type, Map<Class<?>, T> map) {
		return map
				.keySet()
				.stream()
				.filter(base -> base.isAssignableFrom(type))
				.findAny()
				.map(map::get);
	}

	/**
	 * Generates a transformer from a factory supporting the given type.
	 * @param type Type
	 * @return Generated transformer
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Optional<Transformer> factory(Class<?> type) {
		return find(type, factories).map(factory -> factory.transformer((Class) type));
	}

	/**
	 * Registers a derived transformer as a side-effect.
	 */
	@SuppressWarnings("rawtypes")
	private void register(Class<?> type, Optional<Transformer> transformer) {
		transformer.ifPresent(t -> registry.put(type, t));
	}

	/**
	 * Registers the transformer for the given type.
	 * @param <T> Type
	 * @param type				Type
	 * @param transformer		Transformer
	 */
	public <T> void register(Class<T> type, Transformer<? extends T, ?> transformer) {
		requireNonNull(type);
		requireNonNull(transformer);
		registry.put(type, transformer);
	}

	/**
	 * Registers a transformer factory for the given type.
	 * @param <T> Type
	 * @param type			Type
	 * @param factory		Transformer factory
	 */
	public <T> void register(Class<T> type, Factory<? extends T> factory) {
		requireNonNull(type);
		requireNonNull(factory);
		factories.put(type, factory);
	}
}
