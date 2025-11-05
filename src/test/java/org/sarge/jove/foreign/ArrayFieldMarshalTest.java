package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class ArrayFieldMarshalTest {
	@SuppressWarnings("rawtypes")
	private Transformer transformer;
	private SegmentAllocator allocator;
	private ArrayFieldMarshal marshal;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new PrimitiveTransformer<>(JAVA_INT).array();
		marshal = new ArrayFieldMarshal(0, 8, int.class, 2);
	}

	@Test
	void marshal() {
		final MemorySegment address = allocator.allocate(8);
		marshal.marshal(new int[]{2, 3}, transformer, address, allocator);
		assertEquals(2, address.getAtIndex(JAVA_INT, 0));
		assertEquals(3, address.getAtIndex(JAVA_INT, 1));
	}

	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocate(8);
		address.setAtIndex(JAVA_INT, 0, 2);
		address.setAtIndex(JAVA_INT, 1, 3);

		final var array = (int[]) marshal.unmarshal(address, transformer);
		assertArrayEquals(new int[] {2, 3}, array);
	}
}
