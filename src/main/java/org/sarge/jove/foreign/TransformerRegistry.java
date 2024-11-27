package org.sarge.jove.foreign;

import java.util.*;

import org.sarge.jove.common.Handle.HandleNativeTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.IntegerReference.IntegerReferenceTransform;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;
import org.sarge.jove.foreign.PointerReference.PointerReferenceTransform;
import org.sarge.jove.util.*;

/**
 * The <i>native mapper registry</i> is used to lookup supported native mappers.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
public class TransformerRegistry {
	private final Map<Class<?>, NativeTransformer> registry = new HashMap<>();
	private final NativeTransformer array = new ArrayNativeTransformer(this);

	/**
	 * Registers a native transformer.
	 * @param transformer Native transformer
	 * @throws IllegalArgumentException if the type of the given mapper is {@code null}
	 */
	public void add(NativeTransformer transformer) {
		final Class<?> type = transformer.type();
		if(type == null) throw new IllegalArgumentException("Transformer must have a Java type: " + transformer);
		registry.put(type, transformer);
	}

	/**
	 * Looks up the native transformer for the given Java type.
	 * @param type Java type
	 * @return Transformer
	 * @throws IllegalArgumentException if the type is not supported by this registry
	 */
	public NativeTransformer get(Class<?> type) {
		if(type.isArray() || Collection.class.isAssignableFrom(type)) {
			return array;
		}

		final NativeTransformer transform = registry.get(type);
		if(transform == null) {
			return find(type);
		}
		else {
			return transform;
		}
	}

	/**
	 * Finds a native transformer for the given subclass.
	 */
	@SuppressWarnings("unchecked")
	private NativeTransformer find(Class<?> type) {
		return registry
				.values()
				.stream()
				.filter(e -> e.type().isAssignableFrom(type))
				.findAny()
				.map(m -> derive(m, type))
				.orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + type));
	}

	/**
	 * Derives a native transformer for the given subclass and registers as a side-effect.
	 * @param transformer	Super-type transformer
	 * @param type		Subclass type
	 * @return Derived mapper
	 */
	private NativeTransformer derive(NativeTransformer transformer, Class<?> type) {
		@SuppressWarnings("unchecked")
		final NativeTransformer derived = transformer.derive(type, this);
		add(derived);
		return derived;
	}

	/**
	 * Creates a registry with support for all standard built-in types.
	 * @return Default mapper registry
	 */
	public static TransformerRegistry create() {
		// Register primitive types
		final var registry = new TransformerRegistry();
		PrimitiveNativeTransformer.mappers().forEach(registry::add);

		// Register reference types
		final NativeTransformer[] mappers = {
				new IntEnumNativeTransformer(),
				new BitMaskNativeTransformer(),
				new StringNativeTransformer(),
				new HandleNativeTransformer(),
				new NativeObjectTransformer(),
				new IntegerReferenceTransform(),
				new PointerReferenceTransform(),
				new StructureNativeTransformer(),
		};
		for(var m : mappers) {
			registry.add(m);
		}

		return registry;
	}
}
