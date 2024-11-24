package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeMapperTest {
	private StringNativeMapper mapper;
	private NativeMapperRegistry registry;
	private NativeContext context;
	private String string;

	@BeforeEach
	void before() {
		string = "string";
		mapper = new StringNativeMapper();
		registry = new NativeMapperRegistry();
		registry.add(mapper);
		context = new NativeContext(Arena.ofAuto(), registry);
	}

	@Test
	void mapper() {
		assertEquals(String.class, mapper.type());
		assertEquals(ValueLayout.ADDRESS, mapper.layout(null));
	}

	@DisplayName("A string can be marshalled to off-heap memory")
	@Test
	void marshal() {
		final MemorySegment segment = mapper.marshal(string, context);
		assertEquals(string, segment.getString(0));
	}

	@DisplayName("Strings marshalled to the native layer are cached")
	@Test
	void cached() {
		final MemorySegment segment = mapper.marshal(string, context);
		assertSame(segment, mapper.marshal(string, context));
	}

	@DisplayName("A marshalled string with an expired context is removed from the cache")
	@Test
	void expired() {
		final MemorySegment segment;
		try(Arena arena = Arena.ofConfined()) {
			final var temp = new NativeContext(arena, registry);
			segment = mapper.marshal(string, temp);
		}
		assertNotSame(segment, mapper.marshal(string, context));
	}

	@DisplayName("A null string can be marshalled")
	@Test
	void marshalNull() {
		assertEquals(MemorySegment.NULL, mapper.marshalNull(String.class));
	}

	@DisplayName("A string can be returned from a native method")
	@Test
	void returns() {
		final MemorySegment address = Arena.ofAuto().allocateFrom(string);
		assertEquals(string, mapper.returns(String.class).apply(address));
	}
}
