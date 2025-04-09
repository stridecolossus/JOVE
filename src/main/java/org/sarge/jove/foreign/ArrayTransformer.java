package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
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

		// TODO
		// we should be switching on the TRANSFORMER not the layout
		// but nasty casts...

		switch(layout) {
			// should be default REFERENCE transformer
    		case AddressLayout _ -> {
//        		for(int n = 0; n < length; ++n) {
//        			final MemorySegment element = (MemorySegment) TransformerHelper.marshal(Array.get(arg, n), transformer, allocator);
//        			if(MemorySegment.NULL.equals(element)) {
//        				continue;
//        			}
//        			address.setAtIndex(ValueLayout.ADDRESS, n, element);
//        		}
    			final var handle = MethodHandles.insertCoordinates(layout.arrayElementVarHandle(), 1, 0L);
        		for(int n = 0; n < length; ++n) {
        			final MemorySegment element = (MemorySegment) TransformerHelper.marshal(Array.get(arg, n), transformer, allocator);
        			handle.set(address, (long) n, element);
        		}
    		}

    		// should be STRUCTURE transformer
    		case StructLayout _ -> {
    			final Object[] array = (Object[]) arg;
    			final long size = transformer.layout().byteSize();
        		for(int n = 0; n < array.length; ++n) {
        			// TODO - this has to be wrong
        			// already allocated address above
        			// the helper then creates another one
        			// and we than copyFrom()
        			// should use other marshal(address?) method
        			// i.e...
//        				final var st = (StructureTransformer) transformer;							// nasty cast #1
//        				final MemorySegment element = address.asSlice(n * size, size);
//        				st.marshal((NativeStructure) array[n], element, allocator);					// nasty cast #2
        			// ....


        			final MemorySegment element = (MemorySegment) TransformerHelper.marshal(array[n], transformer, allocator);
        			if(MemorySegment.NULL.equals(element)) {
        				continue;
        			}
        			// TODO - is there an array-like way of doing this? rather than copyFrom() each element?
        			address.asSlice(n * size, size).copyFrom(element);
        		}
    		}

    		// TODO - can we use the arrayElementVarHandle() approach for ALL cases?
    		// but what about structures that access 'flat' memory using copy()?
    		// or wrap up the get/set logic somehow?

    		case ValueLayout type -> {
    			final var handle = MethodHandles.insertCoordinates(layout.arrayElementVarHandle(), 1, 0L);
        		for(int n = 0; n < length; ++n) {
        			handle.set(address, (long) n, Array.get(arg, n));
        		}
    		}

    		default -> throw new UnsupportedOperationException();
		}

		return address;
	}

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
