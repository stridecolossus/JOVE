package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class SliceFieldMarshalTest {
	@SuppressWarnings("rawtypes")
	private Transformer transformer;
	private String string;
	private SegmentAllocator allocator;
	private MemorySegment address;
	private SliceFieldMarshal marshal;

	@BeforeEach
	void before() {
		transformer = new StringTransformer();
		string = "whatever";
		allocator = Arena.ofAuto();
		address = allocator.allocate(string.length() + 1);
		marshal = new SliceFieldMarshal(0, string.length() + 1);
	}

	@Test
	void marshal() {
		marshal.marshal(string, transformer, address, allocator);
		assertEquals(string, address.getString(0L));
	}

	@Test
	void unmarshal() {
		final MemorySegment address = allocator.allocate(string.length() + 1);
		address.setString(0L, string);
		assertEquals(string, marshal.unmarshal(address, transformer));
	}
}
