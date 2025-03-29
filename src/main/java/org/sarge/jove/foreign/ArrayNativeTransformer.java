package org.sarge.jove.foreign;

import java.lang.foreign.*;

public class ArrayNativeTransformer extends AbstractNativeTransformer<Object[]> {
	private final NativeTransformer component;

	/**
	 * Constructor.
	 * @param component
	 */
	public ArrayNativeTransformer(NativeTransformer<?> component) {
		this.component = component;
	}

	@Override
	public MemorySegment marshal(Object[] array, SegmentAllocator allocator) {
		final MemoryLayout layout = component.layout();
		final MemorySegment address = allocator.allocate(layout, array.length);

		if(ValueLayout.ADDRESS.equals(layout)) {
    		for(int n = 0; n < array.length; ++n) {
    			final MemorySegment element = (MemorySegment) component.marshal(array[n], allocator);
    			address.setAtIndex(ValueLayout.ADDRESS, n, element);
    		}
		}
		else {
    		final long size = layout.byteSize();

    		for(int n = 0; n < array.length; ++n) {
    			final MemorySegment element = (MemorySegment) component.marshal(array[n], allocator);
    			final MemorySegment slice = address.asSlice(n * size, size);
    			slice.copyFrom(element);
    		}
    	}

		return address;
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
