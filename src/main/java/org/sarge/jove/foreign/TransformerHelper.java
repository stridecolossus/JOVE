package org.sarge.jove.foreign;

import java.lang.foreign.SegmentAllocator;

/**
 *
 * @author Sarge
 */
final class TransformerHelper {
	private TransformerHelper() {
	}

	/**
	 * Marshals a method argument.
	 * @param arg				Argument
	 * @param transformer		Transformer
	 * @param allocator			Allocator
	 * @return Marshalled argument
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object marshal(Object arg, Transformer transformer, SegmentAllocator allocator) {
		return switch(transformer) {
        	case IdentityTransformer _ -> arg;

        	case DefaultTransformer def -> {
        		if(arg == null) {
        			yield def.empty();
        		}
        		else {
        			yield def.marshal(arg, allocator);
        		}
        	}
		};
	}

	/**
	 * Unmarshals from off-heap memory.
	 * @param value				Off-heap value
	 * @param transformer		Transformer
	 * @return Unmarshalled value
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object unmarshal(Object value, Transformer transformer) {
		return switch(transformer) {
			case IdentityTransformer _ -> value;
			case DefaultTransformer def -> def.unmarshal().apply(value);
		};
	}
}
