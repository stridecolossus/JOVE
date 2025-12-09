package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class DefaultArrayTransformerTest {
	private SegmentAllocator allocator;
	private DefaultArrayTransformer transformer;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		transformer = new DefaultArrayTransformer(new StringTransformer());
	}

	@Test
	void layout() {
		assertEquals(ADDRESS, transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@Test
	void marshal() {
		final String string = "string";
		final MemorySegment address = transformer.marshal(new String[]{string}, allocator);
		final MemorySegment element = address.getAtIndex(ADDRESS, 0);
		assertEquals(string, element.reinterpret(Integer.MAX_VALUE).getString(0L));
	}

	@Test
	void unmarshal() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
	}

	@Test
	void update() {
		final String string = "string";
		final MemorySegment address = allocator.allocate(ADDRESS, 1);
		address.setAtIndex(ADDRESS, 0L, allocator.allocateFrom(string));

		final String[] array = new String[1];
		transformer.update().accept(address, array);
		assertEquals(string, array[0]);
	}

	@Test
	void updateNullElement() {
		final MemorySegment address = allocator.allocate(ADDRESS, 1);
		final String[] array = new String[1];
		transformer.update().accept(address, array);
		assertEquals(null, array[0]);
	}
}
