package org.sarge.jove.lib;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.*;

/**
 * A <i>native structure</i> represents a native structure or union.
 * TODO
 * - public fields
 * - mapping to layout
 * - modify issues, i.e. ignored after allocated!!!
 * @author Sarge
 */
public abstract class NativeStructure {
	/**
	 * Memory layout for a pointer field of a structure.
	 */
    protected static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));

    /**
     * Default padding to ensure structure fields are correctly aligned.
     */
    protected static final MemoryLayout PADDING = MemoryLayout.paddingLayout(4);

    private final Pointer pointer = new Pointer();

    /**
	 * @return Memory layout of this structure
	 */
	protected abstract StructLayout layout();

	/**
	 * The <i>structure native mapper</i> marshals a {@link NativeStructure} to/from its native representation.
	 */
	public static class StructureNativeMapper extends DefaultNativeMapper<NativeStructure, MemorySegment> {
		/**
		 * Structure metadata.
		 */
		private record Entry(StructLayout layout, List<FieldMapping> mappings) {
		}

		private final Map<Class<? extends NativeStructure>, Entry> entries = new HashMap<>();
		private final NativeMapperRegistry registry;

	    /**
		 * Constructor.
		 * @param registry Native mappers
		 */
		public StructureNativeMapper(NativeMapperRegistry registry) {
			super(NativeStructure.class, ValueLayout.ADDRESS);
			this.registry = requireNonNull(registry);
		}

		/**
		 * Retrieves the metadata for the given structure.
		 */
		private Entry entry(NativeStructure structure) {
			return entries.computeIfAbsent(structure.getClass(), __ -> create(structure));
		}

		/**
		 * Builds the metadata for the given structure.
		 */
		private Entry create(NativeStructure structure) {
			final StructLayout layout = structure.layout();
			final Class<? extends NativeStructure> type = structure.getClass();
			final List<FieldMapping> mappings = FieldMapping.build(layout, type, registry);
			return new Entry(layout, mappings);
		}

		@Override
		public MemorySegment toNative(NativeStructure structure, NativeContext context) {
			final Pointer pointer = structure.pointer;
			if(!pointer.isAllocated()) {
				populate(structure, context);
			}
			return pointer.address();
		}

		/**
		 * Marshals the given structure to off-heap memory.
		 */
		private void populate(NativeStructure structure, NativeContext context) {
			// Allocate off-heap structure memory
			final Entry entry = entry(structure);
			final MemorySegment address = structure.pointer.allocate(entry.layout, context);

			// Populate off-heap memory from structure
			for(FieldMapping m : entry.mappings) {
				m.toNative(structure, address, context);
			}
		}

		@Override
		public Object fromNative(MemorySegment address, Class<? extends NativeStructure> type) {
			// Create new structure and lookup metadata
			final var structure = create(type);
			final Entry entry = entry(structure);

			// Populate structure from off-heap memory
			final MemorySegment pointer = address.reinterpret(entry.layout.byteSize());
			for(FieldMapping m : entry.mappings) {
				m.fromNative(pointer, structure);
			}
			// TODO - reset pointer?

			return structure;
		}

		/**
		 * Instantiates a new structure instance of the given type.
		 */
		private static NativeStructure create(Class<? extends NativeStructure> type) {
			try {
				return type.getDeclaredConstructor().newInstance();
			}
			catch(Exception e) {
				throw new RuntimeException("Error creating structure return value: " + type, e);
			}
		}
	}
}
