package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
class StringTransformerTest {
	private StringTransformer transformer;
	private SegmentAllocator allocator;
	private String string;

	@BeforeEach
	void before() {
		string = "whatever";
		allocator = Arena.ofAuto();
		transformer = new StringTransformer();
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@Test
	void marshal() {
		final MemorySegment address = transformer.marshal(string, allocator);
		assertEquals(string, address.getString(0));
	}

	@SuppressWarnings("static-access")
	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocateFrom(string);
		assertEquals(string, transformer.unmarshal(address));
	}

	@DisplayName("A string can be unmarshalled from an off-heap sequence")
	@SuppressWarnings("static-access")
	@Test
	void segment() {
		final var layout = MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_BYTE);
		final MemorySegment address = allocator.allocate(layout);
		address.setString(0, string);
		assertEquals(string, transformer.unmarshal(address));
	}

	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}

	@Test
	void array() {
		final MemorySegment address = allocator.allocate(ValueLayout.ADDRESS, 2);
		address.setAtIndex(ValueLayout.ADDRESS, 0L, allocator.allocateFrom(string));
		address.setAtIndex(ValueLayout.ADDRESS, 1L, allocator.allocateFrom(string));
		assertArrayEquals(new String[]{string, string}, StringTransformer.array(address, 2));
	}
}
