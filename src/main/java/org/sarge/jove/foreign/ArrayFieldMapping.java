package org.sarge.jove.foreign;

import java.lang.foreign.*;

/**
 * TODO
 */
public record ArrayFieldMapping() implements FieldMapping {
	@Override
	public void marshal(Object value, MemorySegment address, SegmentAllocator allocator) {
	}

	@Override
	public Object unmarshal(MemorySegment address, NativeStructure structure) {
		return null;
	}
}
