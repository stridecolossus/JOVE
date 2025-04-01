package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>transformer helper</i> marshals Java types to/from the native layer.
 * @author Sarge
 */
class TransformerHelper {
	private TransformerHelper() {
	}

	/**
	 * Marshals a Java argument to its native equivalent.
	 * @param arg				Java argument
	 * @param transformer		Transformer
	 * @param allocator			Off-heap allocator
	 * @return Native argument
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object marshal(Object arg, Transformer transformer, SegmentAllocator allocator) {
		return switch(transformer) {
			case Primitive _ -> arg;
			case ReferenceTransformer ref -> {
				if(arg == null) {
					yield MemorySegment.NULL;
				}
				else {
					yield ref.marshal(arg, allocator);
				}
			}
		};
	}

	/**
	 * Extracts the transformation function for a native return value.
	 * @param transformer Return value transformer
	 * @return Return value function
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Function<Object, Object> unmarshal(Transformer transformer) {
		return switch(transformer) {
			case null							-> Function.identity();
			case Primitive _					-> Function.identity();
			case ReferenceTransformer def		-> def.unmarshal();
		};
	}
}
