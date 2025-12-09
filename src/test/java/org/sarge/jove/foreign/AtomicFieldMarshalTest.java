package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

import org.junit.jupiter.api.*;

class AtomicFieldMarshalTest {
	@SuppressWarnings("rawtypes")
	private Transformer transformer;
	private MemorySegment address;
	private SegmentAllocator allocator;
	private AtomicFieldMarshal marshal;

	@BeforeEach
	void before() {
		final PathElement path = PathElement.groupElement("field");
		final VarHandle handle = MockStructure.LAYOUT.varHandle(path);
		marshal = new AtomicFieldMarshal(handle);
		transformer = new PrimitiveTransformer<>(JAVA_INT);
		allocator = Arena.ofAuto();
		address = allocator.allocate(MockStructure.LAYOUT);
	}

	@Test
	void marshal() {
		marshal.marshal(2, transformer, address, allocator);
		assertEquals(2, address.get(JAVA_INT, 0L));
	}

	@Test
	void unmarshal() {
		address.set(JAVA_INT, 0L, 3);
		assertEquals(3, marshal.unmarshal(address, transformer));
	}
}
