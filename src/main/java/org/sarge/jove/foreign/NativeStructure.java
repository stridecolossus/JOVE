package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.List;
import java.util.function.*;

/**
 * A <i>native structure</i> represents a native structure or union.
 * TODO
 * - public fields
 * - mapping to layout
 * - modify issues, i.e. ignored after allocate => (re)marshals every time
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
	 * The <i>structure native mapper</i> marshals a {@link NativeStructure} to/from its native representation.
	 */
	class StructureNativeTransformer extends AbstractNativeTransformer<NativeStructure, MemorySegment> {
		private final TransformerRegistry registry;

		/**
		 * Constructor.
		 * @param registry Native transformers
		 */
		public StructureNativeTransformer(TransformerRegistry registry) {
			this.registry = requireNonNull(registry);
		}

		@Override
		public NativeTransformer<NativeStructure, MemorySegment> derive(Class<? extends NativeStructure> target) {
			return new Instance(target);
		}

		@Override
		public Class<? extends NativeStructure> type() {
			return NativeStructure.class;
		}

		@Override
		public MemorySegment transform(NativeStructure instance, SegmentAllocator allocator) {
			throw new RuntimeException();
		}

    	/**
    	 * Structure subclass mapper.
    	 */
    	private class Instance extends AbstractNativeTransformer<NativeStructure, MemorySegment> {
    		private final Class<? extends NativeStructure> type;
    		private final StructLayout layout;
    		private final List<FieldMapping> mappings;

    		/**
    		 * Constructor.
    		 * @param type Structure type
    		 */
    		public Instance(Class<? extends NativeStructure> type) {
    			this.type = requireNonNull(type);
    			this.layout = create(type).layout();
    			this.mappings = FieldMapping.build(layout, type, registry);
    		}

    		@Override
    		public Class<? extends NativeStructure> type() {
    			return type;
    		}

    		@Override
    		public MemoryLayout layout() {
    			return layout;
    		}

    		@Override
    		public MemorySegment transform(NativeStructure structure, SegmentAllocator allocator) {
    			final MemorySegment address = allocator.allocate(structure.layout());
    			transform(structure, address, allocator);
    			return address;
    		}

    		// TODO - only used for this one case
    		@Override
			public void transform(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
    			for(var m : mappings) {
    				m.marshal(structure, address, allocator);
    			}
    		}

    		@Override
    		public Function<MemorySegment, NativeStructure> returns() {
    			return address -> {
    				final NativeStructure structure = create(type);
    				unmarshal(address, structure);
    				return structure;
    			};
    		}

    		@Override
    		public BiConsumer<MemorySegment, NativeStructure> update() {
    			return this::unmarshal;
    		}

    		/**
    		 * Unmarshals off-heap memory to the given structure.
    		 */
    		private void unmarshal(MemorySegment address, NativeStructure structure) {
    			for(var m : mappings) {
    				m.unmarshal(address, structure);
    			}
    		}

    		/**
    		 * Instantiates a new structure instance of the given type.
    		 */
    		private static NativeStructure create(Class<? extends NativeStructure> type) {
    			try {
    				return type.getDeclaredConstructor().newInstance();
    			}
    			catch(Exception e) {
    				throw new RuntimeException("Error instantiating structure: " + type, e);
    			}
    		}
    	}
    }
}
