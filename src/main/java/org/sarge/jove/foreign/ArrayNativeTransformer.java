package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.BiConsumer;

/**
 *
 * @author Sarge
 */
public class ArrayNativeTransformer extends AbstractNativeTransformer<Object[], MemorySegment> {
	private final TransformerRegistry registry;

	/**
     * Constructor.
     * @param registry
     */
    public ArrayNativeTransformer(TransformerRegistry registry) {
    	this.registry = requireNonNull(registry);
    }

	@Override
	public Class<Object[]> type() {
		return Object[].class;
	}

	@SuppressWarnings("rawtypes")
	private NativeTransformer mapper(Object[] array) {
		final Class<?> component = array.getClass().getComponentType();
		return registry.get(component);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object transform(Object[] array, SegmentAllocator allocator) {
		// Allocate off-heap memory for this array
		final NativeTransformer mapper = mapper(array);
		final MemorySegment address = allocator.allocate(mapper.layout(), array.length);

		// Marshal array elements
		for(int n = 0; n < array.length; ++n) {
			// Skip if empty
			if(array[n] == null) {
				continue;
			}

			// Marshal array element
			@SuppressWarnings("unchecked")
			final MemorySegment element = (MemorySegment) mapper.transform(array[n], allocator);

			// Skip if empty
			if(MemorySegment.NULL.equals(element)) {
				continue;
			}

			// Populate off-heap array element
			// TODO - switch(layout) as below, need to overwrite structure address with slice?
			address.setAtIndex(ADDRESS, n, element);
		}

		return address;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public BiConsumer<MemorySegment, Object[]> update() {
		return (address, array) -> {
			final NativeTransformer mapper = mapper(array);
    		final MemoryLayout layout = mapper.layout();
    		for(int n = 0; n < array.length; ++n) {			// TODO - loop per case? wrap into some helper function? nasty
    			// Extract array element
    			final MemorySegment element = switch(layout) {
    				case AddressLayout __ -> address.getAtIndex(ADDRESS, n);
    				case ValueLayout __ -> throw new UnsupportedOperationException(); // TODO - primitive array
    				case StructLayout struct -> address.asSlice(n * struct.byteSize(), struct);
    				default -> throw new RuntimeException();
    			};

    			// Skip if empty
    			if(MemorySegment.NULL.equals(element)) {
    				continue;
    			}

    			// Unmarshal array element
    			if(array[n] == null) {
    				array[n] = mapper.returns().apply(element);
    			}
    			else {
    				mapper.update().accept(element, array[n]);
    			}
    		}
		};
	}
}
