package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.List;
import java.util.function.*;

import org.sarge.jove.foreign.DefaultArrayTransformer.ElementAccessor;

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
	 * @param factory		Factory for new instances
	 * @param layout		Structure layout
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
//		for(FieldMapping f : mappings) {
//			f.marshal(structure, address, allocator);
//		}
		marshal(structure, address, allocator);
		return address;
	}

	protected void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
		for(FieldMapping f : mappings) {
			f.marshal(structure, address, allocator);
		}
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

	/**
	 * Unmarshals the given structure from off-heap memory.
	 * @param address		Off-heap memory
	 * @param structure		Structure to update
	 */
	private void update(MemorySegment address, NativeStructure structure) {
		for(FieldMapping f : mappings) {
			// TODO
			try {
				f.unmarshal(address, structure);
			}
			catch(Exception e) {
				System.out.println("mapping="+f+" structure="+structure);
				throw e;
			}
		}
	}

	/**
	 * Accesses off-heap elements using memory slicing.
	 */
	private class StructureElementAccessor implements ElementAccessor {
		private final long size = layout.byteSize();

		@Override
		public void set(MemorySegment address, int index, Object element) {
			final MemorySegment slice = get(address, index);
			slice.copyFrom((MemorySegment) element);
		}

		@Override
		public MemorySegment get(MemorySegment address, int index) {
			return address.asSlice(index * size, size);
		}
	}

	@Override
	public Transformer<?, ?> array() {
		return new DefaultArrayTransformer(this, new StructureElementAccessor());
	}
}
