package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class StringNativeTransformerTest {
	private StringNativeTransformer transformer;
	private TransformerRegistry registry;
	private Arena arena;
	private String string;

	@BeforeEach
	void before() {
		string = "string";
		transformer = new StringNativeTransformer();
		registry = new TransformerRegistry();
		registry.add(transformer);
		arena = Arena.ofAuto();
	}

	@Test
	void mapper() {
		assertEquals(String.class, transformer.type());
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
		assertEquals(transformer, transformer.derive(null, null));
	}

	@DisplayName("A string can be transformed to a native address")
	@Test
	void transform() {
		final MemorySegment segment = transformer.transform(string, arena);
		assertEquals(string, segment.getString(0));
	}

//	@DisplayName("Strings marshalled to the native layer are cached")
//	@Test
//	void cached() {
//		final MemorySegment segment = transformer.transform(string, arena);
//		assertSame(segment, transformer.transform(string, arena));
//	}

	@DisplayName("A null string can be transformed")
	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty());
	}

	@DisplayName("A string can be returned from a native method")
	@Test
	void returns() {
		final MemorySegment address = arena.allocateFrom(string);
		assertEquals(string, transformer.returns().apply(address));
	}
}
