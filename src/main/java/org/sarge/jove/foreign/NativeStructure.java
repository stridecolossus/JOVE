package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

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
	 * The <i>structure transformer</i> marshals a structure to/from off-heap memory.
	 */
	final class StructureTransformer implements ReferenceTransformer<NativeStructure> {
		private static final Logger LOG = Logger.getLogger(StructureTransformer.class.getName());

		/**
		 * Creates a transformer for the given structure.
		 * @param type		Structure type
		 * @param mapper	Type mapper
		 * @return Structure transformer
		 * @throws IllegalArgumentException if any structure field is unsupported
		 */
		public static StructureTransformer create(Class<? extends NativeStructure> type, Registry registry) {
			LOG.info("Generating native structure mapping: " + type);
			final Constructor<? extends NativeStructure> constructor = constructor(type);
			final NativeStructure structure = create(constructor);
			final StructLayout layout = structure.layout();
			final List<FieldMapping> mappings = FieldMapping.build(type, layout, registry);
			return new StructureTransformer(constructor, layout, mappings);
		}

		/**
		 * @return Default constructor for the given structure type
		 */
		private static Constructor<? extends NativeStructure> constructor(Class<? extends NativeStructure> type) {
	    	try {
	    		return type.getConstructor();
	    	}
	    	catch(Exception e) {
	    		throw new RuntimeException("Cannot find default constructor for structure: " + type, e);
	    	}
		}

		/**
		 * Creates a new structure instance.
		 * @param constructor Structure constructor
		 * @return New structure
		 * @throws RuntimeException if the structure cannot be created
		 */
		private static NativeStructure create(Constructor<? extends NativeStructure> constructor) {
			try {
				return constructor.newInstance();
			}
			catch(Exception e) {
				throw new RuntimeException("Error instantiating structure: " + constructor, e);
			}
		}

		private final Constructor<? extends NativeStructure> constructor;
		private final StructLayout layout;
		private final List<FieldMapping> mappings;

		/**
		 * Constructor.
		 * @param constructor		Constructor for new structure instances
		 * @param layout			Memory layout
		 * @param mappings			Field mappings
		 */
		private StructureTransformer(Constructor<? extends NativeStructure> constructor, StructLayout layout, List<FieldMapping> mappings) {
			this.constructor = requireNonNull(constructor);
			this.layout = requireNonNull(layout);
			this.mappings = requireNonNull(mappings);
		}

		@Override
		public MemoryLayout layout() {
			return layout;
		}

		@Override
		public MemorySegment marshal(NativeStructure structure, SegmentAllocator allocator) {
			final MemorySegment address = allocator.allocate(layout);
			marshal(structure, address, allocator);
			return address;
		}

		/**
		 * Marshals a structure to the given off-heap memory.
		 * @param structure		Structure
		 * @param address		Off-heap memory
		 * @param allocator		Off-heap allocator
		 */
		public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
			for(FieldMapping field : mappings) {
				field.marshal(structure, address, allocator);
			}
		}

		@Override
		public Function<MemorySegment, NativeStructure> unmarshal() {
			return address -> {
    			final NativeStructure structure = create(constructor);
    			unmarshal(address, structure);
    			return structure;
			};
		}

		/**
		 * Unmarshals a structure from the given off-heap memory.
		 * @param address		Off-heap memory
		 * @param structure		Structure
		 */
		public void unmarshal(MemorySegment address, NativeStructure structure) {
			for(FieldMapping field : mappings) {
				field.unmarshal(address, structure);
			}
		}
	}
}
