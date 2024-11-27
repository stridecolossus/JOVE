package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

/**
 * An <i>atomic field mapping</i> marshals a primitive or reference structure field using a {@link VarHandle}.
 * @author Sarge
 */
record AtomicFieldMapping(VarHandle handle) implements FieldMapping<Object> {
	@Override
	public void marshal(Object foreign, MemorySegment address, SegmentAllocator allocator) {
		handle.set(address, 0L, foreign);
	}

	@Override
	public Object unmarshal(MemorySegment address, NativeStructure structure) {
		return handle.get(address, 0L);
	}
}
// TODO - insert coordinates
