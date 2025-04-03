package org.sarge.jove.foreign;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure.StructureTransformer;

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
					yield ref.empty();
				}
				else {
					yield ref.marshal(arg, allocator);
				}
			}

			case ArrayTransformer array -> {
				if(arg == null) {
					yield MemorySegment.NULL;
				}
				else {
					yield array.marshal(arg, allocator);
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
	public static Object unmarshal(Object arg, Transformer transformer) {
		return switch(transformer) {
    		case Primitive _				-> arg;
    		case ReferenceTransformer def	-> {
    			if(MemorySegment.NULL.equals(arg)) {
    				yield null;
    			}
    			else {
    				yield def.unmarshal(arg);
    			}
    		}
//    		case ArrayTransformer array		-> array.unmarshal((MemorySegment) arg);
    		case ArrayTransformer array		-> throw new UnsupportedOperationException();
		};
	}

	public static void update(Object foreign, Transformer transformer, Object arg) {
		switch(transformer) {
    		case StructureTransformer struct	-> struct.unmarshal((MemorySegment) foreign, (NativeStructure) arg);
    		case ArrayTransformer array			-> array.update((MemorySegment) foreign, (Object[]) arg);
    		default 							-> throw new RuntimeException();
		}
	}
}
