package org.sarge.jove.foreign;

import java.lang.foreign.*;
import java.util.function.BiConsumer;

class MockStructureTransformer implements Transformer<MockStructure, MemorySegment> {
	@Override
	public MemorySegment marshal(MockStructure arg, SegmentAllocator allocator) {
		return MemorySegment.ofAddress(42);
	}

	@Override
	public BiConsumer<MemorySegment, MockStructure> update() {
		return (_, structure) -> {
			structure.field = 42;
		};
	}
}
