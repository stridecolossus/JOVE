package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.util.*;

/**
 * A <i>registry</i> maps domain types to native transformers used to marshal method parameters and return values.
 * <p>
 * Generally transformers are generated and cached on demand by a {@link Factory} matching the given type.
 * Factories are added to the registry via the {@link #add(Factory)} method.
 * <p>
 * Alternatively the transformer for a given type can be <i>derived</i> from a registered supertype.
 * <p>
 * In either case new transformers are added to the registry via the {@link #add(Class, Transformer)} method.
 * This method can also be used to explicitly register the transformer for a given type.
 * <p>
 * Additionally a registered type is automatically supported as an array with that component type.
 * <p>
 * @author Sarge
 */
public class Registry {
	/**
	 * A <i>transformer factory</i> generates new transformers on demand.
	 * TODO - note used for cases where transformer needs to be class-specific, e.g. enumerations, structures
	 * @param <T> Domain type
	 */
	@FunctionalInterface
	public interface Factory<T> {
		/**
		 * Creates a new transformer for the given type.
		 * @param type Domain type
		 * @return Transformer
		 */
		Transformer<T> create(Class<? extends T> type);
	}

	private final Map<Class<?>, Transformer<?>> registry = new HashMap<>();
	private final Map<Class<?>, Factory<?>> factories = new HashMap<>();

	/**
	 * Looks up or creates the native transformer for the given domain type.
	 * @param type Domain type
	 * @return Native transformer
	 * @throws IllegalArgumentException if the type is not supported
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Transformer<?> transformer(Class<?> type) {
		final Transformer<?> transformer = registry.get(type);

		if(transformer == null) {
			final Transformer created = find(type);
			add(type, created);
			return created;
		}
		else {
			return transformer;
		}
	}

	/**
	 * Finds a transformer for a given type not currently present in this registry.
	 * @param type Domain type
	 * @return Transformer
	 */
	private Transformer<?> find(Class<?> type) {
		// Arrays are automatically handled as a special case
		if(type.isArray()) {
			return array(type);
		}

		// Otherwise generate new transformer
		return create(type)
				.or(() -> derive(type))
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + type));
	}

	/**
	 * @return Array transformer for the given type
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Transformer<?> array(Class<?> type) {
		final Transformer<?> component = find(type.getComponentType());
		final Transformer transformer = new ArrayTransformer<>(component);
		add(type, transformer);
		return transformer;
	}

	/**
	 * Creates a transformer for a type supported by a registered factory.
	 * @param type Domain type
	 * @return New transformer
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Optional<Transformer<?>> create(Class type) {
		return factories
				.keySet()
				.stream()
				.filter(e -> e.isAssignableFrom(type))
				.findAny()
				.map(factories::get)
				.map(factory -> factory.create(type));
	}

	/**
	 * Derives a supertype transformer for the given type.
	 * @param type Java type
	 * @return Supertype transformer
	 */
	private Optional<Transformer<?>> derive(Class<?> type) {
		return registry
				.keySet()
				.stream()
				.filter(e -> e.isAssignableFrom(type))
				.findAny()
				.map(registry::get);
	}

	/**
	 * Registers a native transformer for the given type.
	 * This method can be overridden for the purposes of logging or debugging.
	 * @param <T> Domain type
	 * @param type				Java or domain type
	 * @param transformer		Native transformer
	 */
	public <T> void add(Class<T> type, Transformer<? extends T> transformer) {
		requireNonNull(type);
		requireNonNull(transformer);
		registry.put(type, transformer);
	}

	/**
	 * Registers a transformer factory for the given base type.
	 * @param <T> Domain type
	 * @param type Base domain type
	 * @param factory Transformer factory
	 */
	public <T> void add(Class<T> type, Factory<T> factory) {
		requireNonNull(type);
		requireNonNull(factory);
		factories.put(type, factory);
	}
}
