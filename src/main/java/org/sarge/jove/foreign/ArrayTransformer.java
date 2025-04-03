package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Array;

/**
 *
 * @author Sarge
 */
public final class ArrayTransformer implements Transformer {
	//private final Class<?> component;
	private final Transformer transformer;

	/**
	 * Constructor.
	 * @param component
	 */
	public ArrayTransformer(/*Class<?> component,*/Transformer transformer) {
		//this.component = requireNonNull(component);
		this.transformer = requireNonNull(transformer);
	}

	@Override
	public MemoryLayout layout() {
		return ValueLayout.ADDRESS; // TODO - should this be sequence[component]?
	}

	/**
	 *
	 */
	public MemorySegment marshal(Object arg, SegmentAllocator allocator) {
		final MemoryLayout layout = transformer.layout();
		final int length = Array.getLength(arg);
		final MemorySegment address = allocator.allocate(layout, length);

		switch(layout) {
    		case AddressLayout _ -> {
    			final Object[] array = (Object[]) arg;
        		for(int n = 0; n < length; ++n) {
        			final MemorySegment element = (MemorySegment) TransformerHelper.marshal(array[n], transformer, allocator);
        			if(MemorySegment.NULL.equals(element)) {
        				continue;
        			}
        			address.setAtIndex(ValueLayout.ADDRESS, n, element);
        		}
    		}

    		case StructLayout _ -> {
    			final Object[] array = (Object[]) arg;
    			final long size = transformer.layout().byteSize();
        		for(int n = 0; n < array.length; ++n) {
        			final MemorySegment element = (MemorySegment) TransformerHelper.marshal(array[n], transformer, allocator);
        			if(MemorySegment.NULL.equals(element)) {
        				continue;
        			}
        			// TODO - is there an array-like way of doing this? rather than copyFrom() each element?
        			address.asSlice(n * size, size).copyFrom(element);
        		}
    		}

    		case ValueLayout type -> {
    			final var handle = layout.arrayElementVarHandle();
        		for(int n = 0; n < length; ++n) {
        			handle.set(address, 0L, (long) n, Array.get(arg, n));
        		}
    		}

    		default -> throw new UnsupportedOperationException();
		}

		return address;
	}

//	public Object[] unmarshal(MemorySegment address) {
//		final int count = (int) (address.byteSize() / component.layout().byteSize());
//		final Object[] array = Array.newInstance(component, count);
//
//		Arrays.new
//
//		return null;
//	}

	/**
	 *
	 */
	public void update(MemorySegment address, Object[] array) {
		if(transformer.layout() == ValueLayout.ADDRESS) {
			for(int n = 0; n < array.length; ++n) {
				final MemorySegment element = address.getAtIndex(ValueLayout.ADDRESS, n);
				array[n] = TransformerHelper.unmarshal(element, transformer);
			}
		}
		else {
			final long size = transformer.layout().byteSize();
			for(int n = 0; n < array.length; ++n) {
				final MemorySegment element = address.asSlice(n * size, size);
				array[n] = TransformerHelper.unmarshal(element, transformer);
			}
		}
	}
}
