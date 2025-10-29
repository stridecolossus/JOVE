package org.sarge.jove.foreign;

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
	 * @see IdentityTransformer#primitives(Registry)
	 * @see NativeReference
	 * @see StructureTransformerFactory
	 */
	public static Registry create() {
		// Create registry
		final Registry registry = new Registry();

		// Primitive types
		IdentityTransformer.primitives(registry);
		// TODO - wrappers?

		// Common types
		registry.register(String.class, new StringTransformer());
		registry.register(NativeReference.class, new NativeReferenceTransformer());

		// Enumerations
		registry.register(IntEnum.class, IntEnumTransformer::new);
		registry.register(EnumMask.class, new EnumMaskTransformer());

		// JOVE types
		registry.register(Handle.class, new HandleTransformer());
		registry.register(NativeObject.class, new NativeObjectTransformer());
		registry.register(NativeStructure.class, new StructureTransformerFactory(registry));

		// TODO - returned arrays, or are they handled in registry itself?

		return registry;
	}
}
