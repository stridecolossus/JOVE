package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.function.*;

/**
 * Transformer for a native structure.
 * @author Sarge
 */
public class StructureTransformer implements Transformer<NativeStructure, MemorySegment> {
	private final Supplier<NativeStructure> factory;
	private final GroupLayout layout;
	private final List<FieldMapping> mappings;

	/**
	 * Constructor.
	 * @param factory		Factory for new structure instances
	 * @param layout		Memory layout
	 * @param mappings		Field mappings
	 */
	StructureTransformer(Supplier<NativeStructure> factory, GroupLayout layout, List<FieldMapping> mappings) {
		this.factory = requireNonNull(factory);
		this.layout = requireNonNull(layout);
		this.mappings = List.copyOf(mappings);
	}

	@Override
	public MemoryLayout layout() {
		return layout;
	}

	@Override
	public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
		final MemorySegment address = allocator.allocate(layout);
		// TODO - no need to actually marshal if by-reference parameter
		for(FieldMapping f : mappings) {
			f.marshal(structure, address, allocator);
		}
		return address;
	}

	@Override
	public Function<MemorySegment, NativeStructure> unmarshal() {
		return address -> {
			final NativeStructure structure = factory.get();
			update(address, structure);
			return structure;
		};
	}

	@Override
	public BiConsumer<MemorySegment, NativeStructure> update() {
		return this::update;
	}

	private void update(MemorySegment address, NativeStructure structure) {
		for(FieldMapping f : mappings) {
			f.unmarshal(address, structure);
		}
	}

//	interface ArrayElementAccessor {
//		void marshal(MemorySegment element, int index);
//		void unmarshal(MemorySegment element, int index);
//	}

	@Override
	public Transformer<?, ?> array() {
		return new ArrayTransformer(this) {
			private final long size = transformer.layout().byteSize();

			@Override
			protected void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator) {
// TODO
//				ObjIntConsumer<Object> consumer = (element, index) -> {
//		    		final MemorySegment slice = slice(address, index);
//		    		slice.copyFrom((MemorySegment) element);
//				};

				for(int n = 0; n < length; ++n) {
					final Object value = Array.get(array, n);

					if(value == null) {
						continue;
					}

		    		final var element = (MemorySegment) transformer.marshal(value, allocator);

		    		final MemorySegment slice = slice(address, n);
		    		slice.copyFrom(element);
				}
			}

			@Override
			protected void update(MemorySegment address, Object array, int length) {
				for(int n = 0; n < length; ++n) {
					final MemorySegment element = slice(address, n);

					if(MemorySegment.NULL.equals(element)) {
						continue;
					}

					Array.set(array, n, transformer.unmarshal().apply(element));
				}
			}

			/**
			 * Slices the off-heap memory for an element in this structure array.
			 * @param address		Off-heap memory
			 * @param index			Array index
			 * @return Element slice
			 */
			private MemorySegment slice(MemorySegment address, int index) {
				return address.asSlice(index * size, size);
			}
		};
	}
}
