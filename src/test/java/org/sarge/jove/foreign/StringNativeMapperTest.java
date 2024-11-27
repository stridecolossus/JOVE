package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeMapperTest {
	private StringNativeMapper mapper;
	private NativeMapperRegistry registry;
	private Arena arena;
	private String string;

	@BeforeEach
	void before() {
		string = "string";
		mapper = new StringNativeMapper();
		registry = new NativeMapperRegistry();
		registry.add(mapper);
		arena = Arena.ofAuto();
	}

	@Test
	void mapper() {
		assertEquals(String.class, mapper.type());
		assertEquals(ValueLayout.ADDRESS, mapper.layout());
		assertEquals(mapper, mapper.derive(null, null));
	}

	@DisplayName("A string can be marshalled to off-heap memory")
	@Test
	void marshal() {
		final MemorySegment segment = mapper.marshal(string, arena);
		assertEquals(string, segment.getString(0));
	}

	@DisplayName("Strings marshalled to the native layer are cached")
	@Test
	void cached() {
		final MemorySegment segment = mapper.marshal(string, arena);
		assertSame(segment, mapper.marshal(string, arena));
	}

	@DisplayName("A null string can be marshalled")
	@Test
	void marshalNull() {
		assertEquals(MemorySegment.NULL, mapper.empty());
	}

	@DisplayName("A string can be returned from a native method")
	@Test
	void returns() {
		final MemorySegment address = arena.allocateFrom(string);
		assertEquals(string, mapper.returns().apply(address));
	}
}
