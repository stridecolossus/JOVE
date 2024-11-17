package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeMapperTest {
	private StringNativeMapper mapper;
	private String string;

	@BeforeEach
	void before() {
		mapper = new StringNativeMapper();
		string = "string";
	}

	@Test
	void mapper() {
		assertEquals(String.class, mapper.type());
		assertEquals(ValueLayout.ADDRESS, mapper.layout());
	}

	@Test
	void toNative() {
		final MemorySegment segment = mapper.toNative(string, new NativeContext());
		assertEquals(string, segment.getString(0));
	}

	@Test
	void cached() {
		final MemorySegment segment = mapper.toNative(string, new NativeContext());
		assertSame(segment, mapper.toNative(string, new NativeContext()));
	}

	@Test
	void toNativeNull() {
		assertEquals(MemorySegment.NULL, mapper.toNativeNull(null));
	}

	@Test
	void fromNative() {
		@SuppressWarnings("resource")
		final MemorySegment address = Arena.global().allocateFrom(string);
		assertEquals(string, mapper.fromNative(address, null));
	}
}
