package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.ValueLayout;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Handle.HandleTransformer;
import org.sarge.jove.common.NativeObject.NativeObjectTransformer;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeStructure.StructureTransformerFactory;
import org.sarge.jove.util.*;
import org.sarge.jove.util.EnumMask.EnumMaskTransformer;
import org.sarge.jove.util.IntEnum.IntEnumTransformer;

/**
 * The <i>default registry</i> is a utility class used to register transformers for commonly supported types.
 * @author Sarge
 */
public final class DefaultRegistry {
	private DefaultRegistry() {
	}

	/**
	 * Creates a default registry with transformers for the following common use cases:
	 * <ul>
	 * <li>primitives</li>
	 * <li>strings</li>
	 * <li>enumerations</li>
	 * <li>standard JOVE types, e.g. {@link Handle}</li>
	 * <li>by-reference parameters</li>
	 * <li>structures</li>
	 * </ul>
	 * @return Default registry
	 * @see IdentityTransformer#PRIMITIVES
	 * @see NativeReference
	 * @see StructureTransformerFactory
	 */
	public static Registry create() {
		// Create registry
		final Registry registry = new Registry();

		// Primitive types
		primitives(registry);
		// TODO - wrappers?

		// Common types
		registry.add(String.class, new StringTransformer());
		registry.add(NativeReference.class, new NativeReferenceTransformer());

		// Enumerations
		registry.add(IntEnum.class, IntEnumTransformer::new);
		registry.add(EnumMask.class, new EnumMaskTransformer());

		// JOVE types
		registry.add(Handle.class, new HandleTransformer());
		registry.add(NativeObject.class, new NativeObjectTransformer());
		registry.add(NativeStructure.class, new StructureTransformerFactory(registry));

		// TODO - returned arrays, or are they handled in registry itself?

		return registry;
	}

	/**
	 * Registers transformers for the built-in primitive types.
	 * @param registry Transformer registry
	 */
	@SuppressWarnings("rawtypes")
	private static void primitives(Registry registry) {
		final ValueLayout[] primitives = {
	    		JAVA_BOOLEAN,
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
		};

		for(ValueLayout layout : primitives) {
    		final var transformer = new IdentityTransformer(layout);
			final Class carrier = layout.carrier();
    		registry.add(carrier, transformer);
    	}
    }
	// TODO - also wrappers? otherwise add doc
}
