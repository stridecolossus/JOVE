package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

/**
 * A <i>field mapping</i> composes handles for a structure field and its off-heap counterpart, with a transformer to marshal between the two.
 * @author Sarge
 */
@SuppressWarnings("rawtypes")
record SimpleFieldMapping(VarHandle local, VarHandle foreign, Transformer transformer) implements FieldMapping {
	/**
	 * Constructor.
	 * @param local				Structure field
	 * @param foreign			Off-heap field
	 * @param transformer		Transformer
	 */
	public SimpleFieldMapping {
		requireNonNull(local);
		requireNonNull(foreign);
		requireNonNull(transformer);
	}

	@Override
	public void marshal(NativeStructure structure, MemorySegment address, SegmentAllocator allocator) {
		// Retrieve the structure field
		final Object value = local.get(structure);

		// Skip empty values
		if(value == null) {
			return;
		}

		// Write transformed field
		@SuppressWarnings("unchecked")
		final Object result = transformer.marshal(value, allocator);
		foreign.set(address, result);
	}

	@Override
	public void unmarshal(MemorySegment address, NativeStructure structure) {
		// Retrieve off-heap field
		final Object value = foreign.get(address);

		// Skip empty values
		if(MemorySegment.NULL.equals(value)) {
			return;
		}

		// Update structure field
		@SuppressWarnings("unchecked")
		final Object result = transformer.unmarshal().apply(value);
		local.set(structure, result);
	}
}
