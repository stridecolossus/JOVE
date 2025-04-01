package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeStructure.StructureTransformer;

public class ArrayTransformer implements ReferenceTransformer<Object[]> {
	private final Transformer component;

	/**
	 * Constructor.
	 * @param component
	 */
	public ArrayTransformer(Transformer component) {
		this.component = component;
	}

/*
	public ArrayNativeTransformer byref() {
		return new ArrayNativeTransformer(component) {
			@Override
			public MemorySegment marshal(Object[] array, SegmentAllocator allocator) {
				final MemoryLayout layout = component.layout();
				if(ValueLayout.ADDRESS.equals(layout)) {
					final MemorySegment address = allocator.allocate(layout, array.length);
		    		for(int n = 0; n < array.length; ++n) {
	    				final MemorySegment element;
		    			if(array[n] == null) {
		    				element = allocator.allocate(ValueLayout.ADDRESS);
		    			}
		    			else {
		    				element = (MemorySegment) component.marshal(array[n], allocator);
		    			}
		    			address.setAtIndex(ValueLayout.ADDRESS, n, element);
		    		}
					return null;
				}
				else {
					// TODO
					throw new UnsupportedOperationException();
				}
			}
		};
	}
*/
	// TODO
	// or Pointer[]
	// where Pointer is-a mutable handle
	// Pointer extends Handle?

	// otherwise
	// back to needing pre/post-processing
	// pre to allocate pointers
	// post to replace array elements with Handles

//	public static MemorySegment ARRAY;
//
//	public static void dump() {
//		if(ARRAY == null) {
//			System.out.println("foreign array is NULL");
//		}
//		else {
//			final MemorySegment element = ARRAY.getAtIndex(ADDRESS, 0);
//			System.out.println("address = " + ARRAY);
//			System.out.println("  " + element);
//			System.out.println("  -> " + element.getAtIndex(ADDRESS, 0));
//		}
//	}

	@Override
	public MemorySegment marshal(Object[] array, SegmentAllocator allocator) {

		// TODO
		if(array == null) {
			System.out.println("array is NULL");
			return MemorySegment.NULL;
		}

		final MemoryLayout layout = component.layout();
		final MemorySegment address = allocator.allocate(layout, array.length);

		if(ValueLayout.ADDRESS.equals(layout)) {
    		for(int n = 0; n < array.length; ++n) {
//    			if(array[n] == null) {
//    				System.out.println("skipping NULL"); // element=" + address.getAtIndex(ValueLayout.ADDRESS, n));
//        			final MemorySegment element = allocator.allocate(ValueLayout.ADDRESS);
//        			address.setAtIndex(ValueLayout.ADDRESS, n, element);
//    				continue;
//    			}

        		final MemorySegment element = switch(component) {
        			case Primitive _ -> {
        				throw new UnsupportedOperationException();
        			}

        			case ReferenceTransformer def -> (MemorySegment) def.marshal(array[n], allocator);

        			case StructureTransformer transformer -> {
        				throw new UnsupportedOperationException();
//        				final MemorySegment address = arena.allocate(transformer.layout());
//        				transformer.marshal((NativeStructure) args[n], address, arena);
//        				args[n] = address;
        			}
        		};

        		address.setAtIndex(ValueLayout.ADDRESS, n, element);
    		}
		}
		else {
			throw new UnsupportedOperationException();

//    		final long size = layout.byteSize();
//
//    		for(int n = 0; n < array.length; ++n) {
//    			final MemorySegment element = (MemorySegment) component.marshal(array[n], allocator);
//    			final MemorySegment slice = address.asSlice(n * size, size);
//    			slice.copyFrom(element);
//    		}
    	}

		return address;
	}

	@Override
	public Function<? extends Object, Object[]> unmarshal() {
		throw new UnsupportedOperationException();
	}
}


//// Count number of extensions
//final int length = count.get(JAVA_INT, 0L);
//final String[] array = new String[length];
//
//// Convert to array of strings
//final MemorySegment address = result.reinterpret(length * ADDRESS.byteSize());
//for(int n = 0; n < length; ++n) {
//    final MemorySegment name = address.getAtIndex(ADDRESS, n);
//    array[n] = name.reinterpret(Integer.MAX_VALUE).getString(0L);
//}



// //			//final MemorySegment element = segment.asSlice(n * size, size).get(ValueLayout.ADDRESS, 0L);



//final MemorySegment address = allocate(array, allocator);
//final long size = transformer.layout().byteSize();
//for(int n = 0; n < array.length; ++n) {
//	final MemorySegment element = (MemorySegment) TransformerHelper.transform(array[n], transformer, allocator);
//	final MemorySegment slice = address.asSlice(n * size, size);
//	slice.copyFrom(element);
//}
//return address;
