package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;

import java.lang.foreign.*;
import java.util.*;
import java.util.function.BiConsumer;

/**
 *
 * @author Sarge
 */
public class NativeArrayMapper extends AbstractNativeMapper<Object[], MemorySegment> {
	/**
	 * Array instance wrapper.
	 */
	private record Entry(NativeParameter delegate, MemorySegment address) {
	}

	private final Map<Object[], Entry> cache = new WeakHashMap<>();

	@Override
	public Class<Object[]> type() {
		return Object[].class;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Entry allocate(Object[] array, NativeContext context) {
		// Lookup mapper for the array elements
		final Class<?> component = array.getClass().getComponentType();
		final NativeMapper mapper = context.registry().mapper(component).orElseThrow();

		// Allocate off-heap memory
		final MemoryLayout layout = mapper.layout(component);
		final MemorySegment address = context.allocator().allocate(layout, array.length);

		// Create array wrapper
		final NativeParameter type = new NativeParameter(component, mapper, true);
		return new Entry(type, address);
	}

	@Override
	public Object marshal(Object[] array, NativeContext context) {
		// Allocate off-heap memory for this array
		final Entry entry = cache.computeIfAbsent(array, __ -> allocate(array, context));

		// Marshal array elements
		for(int n = 0; n < array.length; ++n) {
			// Skip if empty
			if(array[n] == null) {
				continue;
			}

			final MemorySegment element = (MemorySegment) entry.delegate.marshal(array[n], context);
			if(Objects.nonNull(element)) {
				// TODO - switch(layout) as below, need to overwrite structure address with slice?
				entry.address.setAtIndex(ADDRESS, n, element);
			}
		}

		return entry.address;
	}

	@Override
	public BiConsumer<MemorySegment, Object[]> unmarshal(Class<? extends Object[]> target) {
		return (address, array) -> {
    		// Retrieve array instance
    		final Entry entry = cache.get(array);
    		if(entry == null) throw new RuntimeException();

    		// Unmarshal array elements
    		final MemoryLayout layout = entry.delegate.layout();
    		for(int n = 0; n < array.length; ++n) {			// TODO - loop per case? wrap into some helper function? nasty

    			final MemorySegment element = switch(layout) {
    				case AddressLayout __ -> address.getAtIndex(ADDRESS, n);
    				case StructLayout struct -> address.asSlice(n * struct.byteSize(), struct);
    				default -> throw new UnsupportedOperationException();
    			};

    			if(array[n] == null) {
    				array[n] = entry.delegate.returns(element);
    			}
    			else {
    				entry.delegate.unmarshal(element, array[n]);
    			}
    		}
		};
	}
}

//    			if(layout instanceof AddressLayout) {
//    				final MemorySegment element = address.getAtIndex(ADDRESS, n);
//    			}
//    			else
//    			if(layout instanceof StructLayout structLayout) {
//    				final MemorySegment element = address.asSlice(n * structLayout.byteSize(), structLayout);
////    				if(array[n] == null) {
////    					array[n] = NativeStructure.create((Class<? extends NativeStructure>) array.getClass().getComponentType());
////    				}
//    				entry.delegate.unmarshal(element, array[n]);
//    			}
//    			else {
//    				// TODO
//    				throw new UnsupportedOperationException();
//    			}
//			}
//		}
