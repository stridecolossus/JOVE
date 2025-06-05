package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.Function;

import org.sarge.jove.foreign.FieldMapping.CompoundFieldMapping;

/**
 * A <i>native structure</i> is the base type for all JOVE structures.
 * @author Sarge
 */
public interface NativeStructure {
	/**
	 * Memory layout for a pointer field of a structure.
	 */
    AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));

    /**
     * Default padding to ensure structure fields are correctly aligned.
     */
    MemoryLayout PADDING = MemoryLayout.paddingLayout(4);

    /**
	 * @return Memory layout of this structure
	 */
	StructLayout layout();

	/**
	 * The <i>structure transformer factory</i> generates a transformer for a given native structure.
	 */
	class StructureTransformerFactory implements Registry.Factory<NativeStructure> {
		private final FieldMapping.Builder builder;

		/**
		 * Constructor.
		 * @param registry Transformer registry
		 */
		public StructureTransformerFactory(Registry registry) {
			this.builder = new FieldMapping.Builder(registry);
		}

		@Override
		public Transformer<NativeStructure> create(Class<? extends NativeStructure> type) {
			return new Transformer<>() {
				private final CompoundFieldMapping mappings = builder.build(type);
        		private final StructLayout layout = mappings.layout();

        		@Override
        		public MemoryLayout layout() {
        			return layout;
        		}

        		@Override
        		public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
        			final MemorySegment address = allocator.allocate(layout);
        			mappings.marshal(structure, address, allocator);
        			return address;
        		}

        		@Override
        		public Function<MemorySegment, NativeStructure> unmarshal() {
        			return mappings::unmarshal;
        		}
            };
		}
    }
}
