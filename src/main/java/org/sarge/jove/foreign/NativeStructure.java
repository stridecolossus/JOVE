package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.Objects;
import java.util.function.*;
import java.util.logging.Logger;

import org.sarge.jove.foreign.FieldMapping.StructureFieldMapping;
import org.sarge.jove.foreign.TransformerRegistry.Factory;

/**
 * A <i>native structure</i> is the base type for all JOVE structures.
 * @see StructureNativeTransformer
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
	 * Transformer for a native structure.
	 * <p>
	 * Implementation considerations for the {@link #layout(Class)} of a native structure:
	 * <ul>
	 * <li>The convenience {@link NativeStructure#POINTER} can be used for pointer fields such as strings, structures, etc</li>
	 * <li>The layout must adhere to the byte alignment requirements of an FFM structure (8 bytes by default), see {@link NativeStructure#PADDING}</li>
	 * <li>A <i>nested</i> structure is declared using an <i>embedded</i> structure layout</li>
	 * </ul>
	 * <p>
	 * Example:
	 * <pre>
	 * TODO
	 * </pre>
	 * <p>
	 * Note that structures (and other data transfer objects) used in a native method are assumed to be transient, carrier types with a scope that is relevant only during method invocation.
	 * Multiple calls to {@link #transform(NativeStructure, ParameterMode, SegmentAllocator)} or other marshalling methods with the same instance will repeat the entire process.
	 * i.e. A native structure has no <i>state</i> or off-heap memory.
	 * <p>
	 */
	record StructureNativeTransformer(Class<? extends NativeStructure> type, StructLayout layout, StructureFieldMapping mapping) implements NativeTransformer<NativeStructure, MemorySegment> {
		private static final Logger LOG = Logger.getLogger(StructureNativeTransformer.class.getName());

		/**
		 * Transformer factory for a native structure.
		 */
		public static final Factory<NativeStructure> FACTORY = (type, registry) -> {
			LOG.info("Registering " + type.getName());
			final NativeStructure structure = create(type);
			final StructLayout layout = structure.layout();
			final var mapping = StructureFieldMapping.build(structure.getClass(), layout, registry);
			return new StructureNativeTransformer(type, layout, mapping);
		};

		/**
		 * Constructor.
		 * @param type			Structure class
		 * @param layout		Memory layout
		 * @param mapping		Field mappings
		 */
		public StructureNativeTransformer {
			requireNonNull(type);
			requireNonNull(layout);
			requireNonNull(mapping);
		}

		@Override
		public MemorySegment transform(NativeStructure structure, ParameterMode mode, SegmentAllocator allocator) {
			final MemorySegment address = allocator.allocate(layout);
			if((mode == ParameterMode.VALUE) && Objects.nonNull(structure)) {
				mapping.transform(structure, address, allocator);
			}
			return address;
		}

		@Override
		public Function<MemorySegment, NativeStructure> returns() {
			return address -> {
				final NativeStructure structure = create(type);
				mapping.populate(address, structure);
				return structure;
			};
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

		@Override
		public BiConsumer<MemorySegment, NativeStructure> update() {
			return (address, structure) -> {
				final var segment = address.reinterpret(layout.byteSize());
				mapping.populate(segment, structure);
			};
		}
	}
}
