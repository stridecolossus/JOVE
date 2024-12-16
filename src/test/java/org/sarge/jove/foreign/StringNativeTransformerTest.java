package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class StringNativeTransformerTest {
	private StringNativeTransformer transformer;
	private Arena arena;
	private String string;

	@BeforeEach
	void before() {
		string = "string";
		transformer = new StringNativeTransformer();
		arena = Arena.ofAuto();
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.ADDRESS, transformer.layout());
	}

	@DisplayName("A string can be transformed to a native address")
	@Test
	void transform() {
		final MemorySegment segment = transformer.transform(string, ParameterMode.VALUE, arena);
		assertEquals(string, segment.getString(0));
	}

	@DisplayName("A null string can be transformed")
	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.transform(null, ParameterMode.VALUE, arena));
	}

	@DisplayName("A string can be returned from a native method")
	@Test
	void returns() {
		final MemorySegment address = arena.allocateFrom(string);
		assertEquals(string, transformer.returns().apply(address));
	}

	@DisplayName("A string cannot be returned as a by-reference argument")
	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}
}
