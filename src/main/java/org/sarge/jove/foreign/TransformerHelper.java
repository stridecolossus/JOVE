package org.sarge.jove.foreign;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;

/**
 * The <i>transformer helper</i> marshals Java types to/from the native layer.
 * TODO - used by native method, arrays and structure transformers when delegating
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
			case IdentityTransformer _ -> arg;

			case AddressTransformer ref -> {
				if(arg == null) {
					yield ref.empty();
				}
				else {
					yield ref.marshal(arg, allocator);
				}
			}

			// TODO - same as address transformer!!!
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

	// TODO - doc
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object unmarshal(Object arg, Transformer transformer) {
		return switch(transformer) {
    		case IdentityTransformer _ -> arg;

    		case AddressTransformer def	-> {
    			if(MemorySegment.NULL.equals(arg)) {
    				yield null;
    			}
    			else {
    				yield def.unmarshal(arg);
    			}
    		}

    		default -> throw new UnsupportedOperationException(); // TODO
		};
	}

//	// TODO - reintroduce update() method on separate generic interface => can then ignore <T> => no daft casting
//	public static void update(Object foreign, Transformer transformer, Object arg) {
//		switch(transformer) {
//    		case StructureTransformer struct	-> struct.update((MemorySegment) foreign, (NativeStructure) arg);
//    		case ArrayTransformer array			-> array.update((MemorySegment) foreign, (Object[]) arg);
//    		default 							-> throw new RuntimeException();
//		}
//	}
}
