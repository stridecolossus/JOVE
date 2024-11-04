package org.sarge.jove.lib;

import java.util.*;

/**
 * The <i>native mapper registry</i> is used to map Java to/from native types.
 * @author Sarge
 */
public class NativeMapperRegistry {
	private final Map<Class<?>, NativeMapper<?>> mappers = new HashMap<>();

	/**
	 * Registers a native mapper.
	 * @param mapper Native mapper
	 */
	public void add(NativeMapper<?> mapper) {
		final Class<?> type = mapper.type();
		if(type == null) throw new NullPointerException();
		mappers.put(type, mapper);
	}

	/**
	 * Looks up the native mapper for the given Java type.
	 * @param type Java type
	 * @return Native mapper
	 */
	public Optional<NativeMapper<?>> mapper(Class<?> type) {

		// TODO - flag for pass-through mapper for primitives

		final NativeMapper<?> mapper = mappers.get(type);
		if(mapper == null) {
			return find(type);
		}
		else {
			return Optional.of(mapper);
		}
	}

	/**
	 * Finds a native mapper for the given super-type and registers the mapping as a side-effect.
	 */
	private Optional<NativeMapper<?>> find(Class<?> type) {
		return mappers
				.values()
				.stream()
				.filter(e -> e.type().isAssignableFrom(type))
				.peek(e -> mappers.put(e.type(), e))
				.findAny();
	}

	/**
	 *
	 * @return
	 */
	public static NativeMapperRegistry create() {

		final var registry = new NativeMapperRegistry();

		// primitives
		// string
		// handle
		// int enum? etc

		registry.add(new IntegerNativeMapper());

		return registry;
	}
}
