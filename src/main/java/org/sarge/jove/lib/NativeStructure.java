package org.sarge.jove.lib;

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
	    private final Map<Class<? extends NativeStructure>, List<FieldMapping>> mappings = new HashMap<>();

	    /**
		 * Constructor.
		 */
		public StructureNativeMapper() {
			super(NativeStructure.class, ValueLayout.ADDRESS);
		}

		@Override
		public MemorySegment toNative(NativeStructure structure, NativeContext context) {
			final Pointer pointer = structure.pointer;
			if(!pointer.isAllocated()) {
				populate(structure, context);
			}
			return pointer.address();
		}

		private void populate(NativeStructure structure, NativeContext context) {
			// Allocate off-heap structure memory
			final StructLayout layout = structure.layout();
			final MemorySegment address = structure.pointer.allocate(layout, context);

			// Build field mappings
			final Class<? extends NativeStructure> type = structure.getClass();
			final List<FieldMapping> map = mappings.computeIfAbsent(type, __ -> FieldMapping.build(layout, type, context.registry()));

			// Populate memory from structure fields
			for(FieldMapping m : map) {
				m.toNative(structure, address, context);
			}
		}

		@Override
		public Object fromNative(MemorySegment value, Class<? extends NativeStructure> type) {
//			final Class<? extends NativeStructure> type = this.getClass();
//			final List<FieldMapping> map = mappings.computeIfAbsent(type, __ -> FieldMapping.build(layout, type, context.registry()));
			// TODO
			throw new UnsupportedOperationException();
		}
	}
}
