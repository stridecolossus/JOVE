package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Array;
import java.util.function.*;

/**
 * An <i>array native transformer</i> marshals a Java array to/from an off-heap memory block.
 * @author Sarge
 */
public class ArrayNativeTransformer implements NativeTransformer<Object, MemorySegment> {
	@SuppressWarnings("rawtypes")
	private final NativeTransformer transformer;
	private final Class<? extends Object> type;

	// https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/lang/foreign/MemorySegment.html#elements(java.lang.foreign.MemoryLayout)

	/**
	 * Constructor.
	 * @param registry Array component transformers
	 */
	@SuppressWarnings("rawtypes")
	public ArrayNativeTransformer(Class<? extends Object> type, NativeTransformer transformer) {
		this.transformer = requireNonNull(transformer);
		this.type = requireNonNull(type);
	}

//	@SuppressWarnings({"rawtypes", "unchecked"})
//	@Override
//	public Class type() {
//		return type;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public MemorySegment transform(Object array, ParameterMode parameter, SegmentAllocator allocator) {

		// TODO - null???

		// Allocate off-heap array
		final int length = Array.getLength(array);
		final Class<?> component = array.getClass().getComponentType();
		final MemoryLayout layout = transformer.layout();
		final MemorySegment address = allocator.allocate(layout, length);

		// Stop if nothing to transform
		if(parameter == ParameterMode.REFERENCE) {
			return address;
		}

//		if(component.isPrimitive()) {
//			System.out.println("primitive "+component);
//		}

		// TODO - transform to on-heap and use MemorySegment.copy()?

		// Copy to off-heap elements
		switch(layout) {
			// TODO - primitives, enums

    		case AddressLayout __ -> {
    			for(int n = 0; n < length; ++n) {
    				final Object e = Array.get(array, n);
    				final MemorySegment foreign = (MemorySegment) transformer.transform(e, parameter, allocator);
    				address.setAtIndex(ADDRESS, n, foreign);
    			}
    		}

    		case ValueLayout value -> {
    			if(layout == ValueLayout.JAVA_FLOAT) {
					for(int n = 0; n < length; ++n) {
						address.setAtIndex(ValueLayout.JAVA_FLOAT, n, Array.getFloat(array, n));
					}
    			}
    			else {
   	    			throw new UnsupportedOperationException();
				}
    		}

    		case StructLayout structure -> {
    			final long size = layout.byteSize();
    			for(int n = 0; n < length; ++n) {
    				final Object e = Array.get(array, n);
    				final MemorySegment foreign = (MemorySegment) transformer.transform(e, parameter, allocator);
    				final MemorySegment slice = address.asSlice(n * size, size);
    				slice.copyFrom(foreign);
    			}

//    			for(int n = 0; n < length; ++n) {
//    				final MemorySegment slice =
//    				slice.copyFrom((MemorySegment) elements[n]);
//    			}
    		}

    		default -> {
    			throw new UnsupportedOperationException();
    		}
		}

		return address;
	}

	@Override
	public Function<MemorySegment, Object> returns() { //Class<? extends Object> target) {
		throw new UnsupportedOperationException();
	}

	// TODO...

	@Override
	public BiConsumer<MemorySegment, Object> update() {
		return (address, array) -> {
			// Determine array properties
			final Class<?> component = array.getClass().getComponentType();
    		final MemoryLayout layout = transformer.layout();
    		final int length = Array.getLength(array);

    		final Function returns = transformer.returns();

    		// Populate array from off-heap elements
    		// TODO
    		switch(layout) {
        		// TODO - primitives, enums

        		case AddressLayout __ -> {
        			for(int n = 0; n < length; ++n) {
        				final MemorySegment segment = address.getAtIndex(ADDRESS, n);
        				Array.set(array, n, returns.apply(segment));
        			}
        		}

        		case StructLayout structure -> {
        			final long size = layout.byteSize();
        			for(int n = 0; n < length; ++n) {
        				final MemorySegment segment = address.asSlice(n * size, size);
        				Array.set(array, n, returns.apply(segment));
        			}
        		}

        		default -> throw new UnsupportedOperationException();
        	}
    	};
	}

	@Override
	public String toString() {
		return String.format("ArrayNativeTransformer[%s]", transformer);
	}
}
