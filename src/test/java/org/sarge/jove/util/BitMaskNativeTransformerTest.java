package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

class BitMaskNativeTransformerTest {
	private BitMaskNativeTransformer transformer;
	private BitMask<?> mask;

	@BeforeEach
	void before() {
		transformer = new BitMaskNativeTransformer();
		mask = new BitMask<>(3);
	}

	@Test
	void constructor() {
		assertEquals(BitMask.class, transformer.type());
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
		assertEquals(transformer, transformer.derive(null));
	}

	@Test
	void transform() {
		assertEquals(3, transformer.transform(mask, null));
	}

	@Test
	void empty() {
		assertEquals(0, transformer.empty());
	}

	@Test
	void returns() {
		assertEquals(mask, transformer.returns().apply(3));
	}

	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}
}
