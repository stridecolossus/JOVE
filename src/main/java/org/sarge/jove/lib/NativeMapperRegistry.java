package org.sarge.jove.lib;

import java.lang.foreign.ValueLayout;
import java.util.*;

import org.sarge.jove.lib.Handle.HandleNativeMapper;
import org.sarge.jove.lib.IntegerReference.IntegerReferenceNativeMapper;
import org.sarge.jove.lib.PointerReference.PointerReferenceNativeMapper;
import org.sarge.jove.lib.StringArray.StringArrayNativeMapper;

/**
 * The <i>native mapper registry</i> is used to lookup supported native mappers.
 * @author Sarge
 */
public class NativeMapperRegistry {
	private final Map<Class<?>, NativeMapper<?>> mappers = new HashMap<>();

	/**
	 * Registers a native mapper.
	 * @param mapper Native mapper
	 * @throws IllegalArgumentException if the type of the given mapper is {@code null}
	 */
	public void add(NativeMapper<?> mapper) {
		final Class<?> type = mapper.type();
		if(type == null) throw new IllegalArgumentException("Native mapper must have a Java type: " + mapper);
		mappers.put(type, mapper);
	}

	/**
	 * Looks up the native mapper for the given Java type.
	 * @param type Java type
	 * @return Native mapper
	 */
	public Optional<NativeMapper<?>> mapper(Class<?> type) {
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
	 * Creates a registry with support for all standard built-in types.
	 * @return Default mapper registry
	 */
	public static NativeMapperRegistry create() {
		final var registry = new NativeMapperRegistry();

		// Register primitive types
		// TODO - constant/helper?
		final var primitives = Map.of(
				byte.class,		ValueLayout.JAVA_BYTE,
				char.class,		ValueLayout.JAVA_CHAR,
				boolean.class,	ValueLayout.JAVA_BOOLEAN,
				int.class,		ValueLayout.JAVA_INT,
				short.class,	ValueLayout.JAVA_SHORT,
				long.class,		ValueLayout.JAVA_LONG,
				float.class,	ValueLayout.JAVA_FLOAT,
				double.class,	ValueLayout.JAVA_DOUBLE
		);
		for(final Class<?> type : primitives.keySet()) {
			final ValueLayout layout = primitives.get(type);
			final var mapper = new DefaultNativeMapper<>(type, layout);
			registry.add(mapper);
		}

		// Register reference types
		final NativeMapper<?>[] reference = {
				new IntEnumNativeMapper(),
				new StringNativeMapper(),
				new HandleNativeMapper(),
				new IntegerReferenceNativeMapper(),
				new PointerReferenceNativeMapper(),
				new StringArrayNativeMapper(),
		};
		for(NativeMapper<?> m : reference) {
			registry.add(m);
		}

		// TODO
		// - structures
		// - arrays?

		return registry;
	}
}
