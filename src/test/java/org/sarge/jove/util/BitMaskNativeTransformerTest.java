package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class BitMaskNativeTransformerTest {
	private BitMaskNativeTransformer transformer;
	private BitMask<?> mask;

	@BeforeEach
	void before() {
		transformer = new BitMaskNativeTransformer();
		mask = new BitMask<>(3);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
	}

	@DisplayName("An enumeration bit-mask is transformed to a native integer")
	@Test
	void transform() {
		assertEquals(3, transformer.transform(mask, ParameterMode.VALUE, null));
	}

	@DisplayName("An empty enumeration bit-mask is transformed to the default zero value")
	@Test
	void empty() {
		assertEquals(0, transformer.transform(null, ParameterMode.VALUE, null));
	}

	@DisplayName("An enumeration bit-mask can be returned from a native method")
	@Test
	void returns() {
		assertEquals(mask, transformer.returns().apply(3));
	}

	@DisplayName("An enumeration bit-mask cannot be returned as a by-reference parameter")
	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}
}
