package org.sarge.jove.foreign;

import java.util.*;

import org.sarge.jove.common.Handle.HandleNativeMapper;
import org.sarge.jove.common.NativeObject.NativeObjectMapper;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceMapper;
import org.sarge.jove.foreign.NativeStructure.StructureNativeMapper;
import org.sarge.jove.foreign.PointerReference.PointerReferenceMapper;
import org.sarge.jove.util.*;

/**
 * The <i>native mapper registry</i> is used to lookup supported native mappers.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class NativeMapperRegistry {
	private final Map<Class<?>, NativeMapper> mappers = new HashMap<>();
	private final NativeMapper array = new NativeArrayMapper(this);

	/**
	 * Registers a native mapper.
	 * @param mapper Native mapper
	 * @throws IllegalArgumentException if the type of the given mapper is {@code null}
	 */
	public void add(NativeMapper mapper) {
		final Class<?> type = mapper.type();
		if(type == null) throw new IllegalArgumentException("Native mapper must have a Java type: " + mapper);
		mappers.put(type, mapper);
	}

	/**
	 * Looks up the native mapper for the given Java type.
	 * @param type Java type
	 * @return Native mapper
	 * @throws IllegalArgumentException if the type is not supported by this registry
	 */
	public NativeMapper mapper(Class<?> type) {
		if(type.isArray() || Collection.class.isAssignableFrom(type)) {
			return array;
		}

		final NativeMapper mapper = mappers.get(type);
		if(mapper == null) {
			return find(type);
		}
		else {
			return mapper;
		}
	}

	/**
	 * Finds a native mapper for the given subclass.
	 */
	@SuppressWarnings("unchecked")
	private NativeMapper find(Class<?> type) {
		return mappers
				.values()
				.stream()
				.filter(e -> e.type().isAssignableFrom(type))
				.findAny()
				.map(m -> derive(m, type))
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + type));
	}

	/**
	 * Derives the native mapper for the given subclass and registers as a side-effect.
	 * @param mapper	Super-type mapper
	 * @param type		Subclass type
	 * @return Derived mapper
	 */
	private NativeMapper derive(NativeMapper mapper, Class<?> type) {
		@SuppressWarnings("unchecked")
		final NativeMapper derived = mapper.derive(type, this);
		if(derived == null) throw new NullPointerException("Native mapper returned null for subclass: " + type);
		mappers.put(type, derived);
		return derived;
	}

	/**
	 * Creates a registry with support for all standard built-in types.
	 * @return Default mapper registry
	 */
	public static NativeMapperRegistry create() {
		// Register primitive types
		final var registry = new NativeMapperRegistry();
		PrimitiveNativeMapper.mappers().forEach(registry::add);

		// Register reference types
		final NativeMapper[] mappers = {
				new IntEnumNativeMapper(),
				new BitMaskNativeMapper(),
				new StringNativeMapper(),
				new HandleNativeMapper(),
				new NativeObjectMapper(),
				new IntegerReferenceMapper(),
				new PointerReferenceMapper(),
				new StructureNativeMapper(),
		};
		for(var m : mappers) {
			registry.add(m);
		}

		return registry;
	}
}
