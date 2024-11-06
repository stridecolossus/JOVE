package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeMapperTest {
	private StringNativeMapper mapper;
	private Arena arena;
	private String string;

	@BeforeEach
	void before() {
		mapper = new StringNativeMapper();
		arena = Arena.ofAuto();
		string = "string";
	}

	@Test
	void type() {
		assertEquals(String.class, mapper.type());
	}

	@Test
	void toNative() {
		final MemorySegment segment = mapper.toNative(string, String.class, arena);
		assertEquals(string, segment.getString(0));
	}

	@Test
	void toNativeNull() {
		assertEquals(MemorySegment.NULL, mapper.toNative(null, String.class, arena));
	}

	@Test
	void fromNative() {
		final MemorySegment segment = arena.allocateFrom(string);
		assertEquals(string, mapper.fromNative(segment, String.class));
	}

	@Test
	void fromNativeNull() {
		assertEquals(null, mapper.fromNative(null, String.class));
		assertEquals(null, mapper.fromNative(MemorySegment.NULL, String.class));
	}
}
