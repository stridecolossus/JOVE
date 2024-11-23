package org.sarge.jove.foreign;

import java.util.*;

import org.sarge.jove.common.Handle.HandleNativeMapper;
import org.sarge.jove.common.NativeObject.NativeObjectMapper;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceNativeMapper;
import org.sarge.jove.foreign.NativeStructure.StructureNativeMapper;
import org.sarge.jove.foreign.PointerReference.PointerReferenceNativeMapper;
import org.sarge.jove.util.*;

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
	 * @throws IllegalArgumentException if the type is not supported by this registry
	 */
	public Optional<NativeMapper<?>> mapper(Class<?> type) {

		if(type.isArray()) {

			// TODO - is this right? shouldn't the code generator decide how Java/JOVE types map to an array?
			if(byte[].class.equals(type)) {
				return Optional.of(new StringNativeMapper());
			}

			// TODO - recurse
			return Optional.of(new NativeArrayMapper());
		}

//		if(Collection.class.isAssignableFrom(type)) {
//			System.out.println("collection "+type);
//			for(var p : type.getTypeParameters()) {
//				System.out.println("name"+p.getName());
//				System.out.println("typename="+p.getTypeName());
//				System.out.println("generic="+p.getGenericDeclaration());
//				System.out.println("bounds="+Arrays.toString(p.getBounds()));
//			}
//			return Optional.of(new NativeArrayMapper());
//		}

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
		final Optional<NativeMapper<?>> mapper = mappers
				.values()
				.stream()
				.filter(e -> e.type().isAssignableFrom(type))
				.findAny();

		mapper.ifPresent(m -> mappers.put(type, m));

		return mapper;
	}

	/**
	 * Creates a registry with support for all standard built-in types.
	 * @return Default mapper registry
	 */
	public static NativeMapperRegistry create() {
		final var registry = new NativeMapperRegistry();

		// Register primitive types
		PrimitiveNativeMapper.mappers().forEach(registry::add);

		// Register reference types
		final NativeMapper<?>[] reference = {
				new IntEnumNativeMapper(),
				new BitMaskNativeMapper(),
				new StringNativeMapper(),
				//new ArrayNativeMapper(),
				new HandleNativeMapper(),
				new NativeObjectMapper(),
				new IntegerReferenceNativeMapper(),
				new PointerReferenceNativeMapper(),
				new StructureNativeMapper(registry),
		};
		for(NativeMapper<?> m : reference) {
			registry.add(m);
		}
		// TODO - arrays?

		return registry;
	}
}
