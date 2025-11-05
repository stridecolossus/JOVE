package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

/**
 * An <i>atomic field marshal</i> marshals a primitive or simple reference type (including structures).
 * @author Sarge
 */
record AtomicFieldMarshal(VarHandle handle) implements FieldMarshal {
	/**
	 * Constructor.
	 * @param handle Off-heap field handle
	 */
	public AtomicFieldMarshal {
		handle = Transformer.removeOffset(handle);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void marshal(Object value, Transformer transformer, MemorySegment address, SegmentAllocator allocator) {
		final Object result = Transformer.marshal(value, transformer, allocator);
		handle.set(address, result);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object unmarshal(MemorySegment address, Transformer transformer) {
		// Retrieve off-heap field value
		final Object value = handle.get(address);

		// Skip empty fields
		if(MemorySegment.NULL.equals(value)) {
			return null;
		}

		// Unmarshal field
		return transformer.unmarshal().apply(value);
	}
}
