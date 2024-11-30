package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.*;

/**
 * An <i>array native transformer</i> marshals a Java array to/from an off-heap memory block.
 * @author Sarge
 */
public class ArrayNativeTransformer extends AbstractNativeTransformer<Object[], MemorySegment> {
	private final TransformerRegistry registry;

	// https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/lang/foreign/MemorySegment.html#elements(java.lang.foreign.MemoryLayout)

	/**
     * Constructor.
     * @param registry
     */
    ArrayNativeTransformer(TransformerRegistry registry) {
    	this.registry = requireNonNull(registry);
    }

	@Override
	public Class<? extends Object[]> type() {
		return Object[].class;
	}

	@Override
	public Object transform(Object[] array, SegmentAllocator allocator) {
		throw new RuntimeException();
	}

	@Override
	public final Function<MemorySegment, Object[]> returns() {
		throw new UnsupportedOperationException("An array cannot be returned from a native method");
	}

	@Override
	public NativeTransformer<? extends Object[], MemorySegment> derive(Class<? extends Object[]> target) {
		// Lookup transformer for this array component
		@SuppressWarnings("rawtypes")
		final NativeTransformer transformer = registry.get(target.getComponentType());
		final MemoryLayout layout = transformer.layout();

		// Retrieve the appropriate element mapper for this array
		final ElementMapper mapper = switch(layout) {
			case AddressLayout __ -> ElementMapper.REFERENCE;
			case ValueLayout __ -> ElementMapper.REFERENCE; // TODO
			case StructLayout structure -> new StructureElementMapper(layout.byteSize());
			default -> throw new RuntimeException();
		};

		// Init derived transformer
		return new AbstractNativeTransformer<>() {
			@Override
			public Class<? extends Object[]> type() {
				return target;
			}

			// TODO - primitive arrays

			@Override
			public Object transform(Object[] array, SegmentAllocator allocator) {
				// Allocate off-heap array
				final MemoryLayout layout = transformer.layout();
				final MemorySegment address = allocator.allocate(layout, array.length);

				// Populate off-heap array
				for(int n = 0; n < array.length; ++n) {
					// Skip if empty
					if(array[n] == null) {
						continue;
					}

					// Transform element and populate off-heap memory
					mapper.marshal(array[n], transformer, address, n, allocator);
				}

				return address;
			}

			@Override
			public BiConsumer<MemorySegment, Object[]> update() {
				return (address, array) -> {
    				for(int n = 0; n < array.length; ++n) {
    					// Extract element
    					final MemorySegment element = mapper.element(address, n);

    					// Skip if empty
    					if(MemorySegment.NULL.equals(element)) {
    						array[n] = null;
    						continue;
    					}

    					// TODO - lookup returns/update methods once
    					// TODO - this is sort of the same problem we have with transform/address for reference/structure types
    					if(array[n] == null) {
    						// Populate new element
    						array[n] = transformer.returns().apply(element);
    					}
    					else {
    						// Unmarshal existing element
    						transformer.update().accept(element, array[n]);
    					}
    				}
    			};
			}
		};
	}

	/**
	 * An <i>element mapper</i> marshals an array to off-heap memory.
	 */
	private interface ElementMapper {
		/**
		 * Retrieves the address of an off-heap array element.
		 * @param address		Memory address
		 * @param index			Array index
		 * @return Element address
		 */
		MemorySegment element(MemorySegment address, int index);

		/**
		 * Marshals an array element to off-heap memory.
		 * TODO - this stinks
		 * @param value
		 * @param transformer
		 * @param address
		 * @param index
		 * @param allocator
		 */
		void marshal(Object value, NativeTransformer transformer, MemorySegment address, int index, SegmentAllocator allocator);

		/**
		 * Default implementation for an array of memory addresses, e.g. a {@code Handle[]}
		 */
		ElementMapper REFERENCE = new ElementMapper() {
			@Override
			public MemorySegment element(MemorySegment address, int index) {
				return address.getAtIndex(ADDRESS, index);
			}

			@Override
			public void marshal(Object value, NativeTransformer transformer, MemorySegment address, int index, SegmentAllocator allocator) {
    			// Transform element
    			@SuppressWarnings("unchecked")
    			final var element = (MemorySegment) transformer.transform(value, allocator);
    			assert Objects.nonNull(element);
    			assert !MemorySegment.NULL.equals(element);

    			// Populate array element
				address.setAtIndex(ADDRESS, index, element);
			}
		};
	}

	/**
	 * Implementation for an array based on a contiguous <i>block</i> of memory such as a structure.
	 */
	private record StructureElementMapper(long size) implements ElementMapper {
		@Override
		public MemorySegment element(MemorySegment address, int index) {
			return address.asSlice(index * size, size);
		}

		@Override
		public void marshal(Object value, NativeTransformer transformer, MemorySegment address, int index, SegmentAllocator allocator) {
			final MemorySegment element = element(address, index);
			transformer.transform(value, element, allocator);
		}
	}
}
