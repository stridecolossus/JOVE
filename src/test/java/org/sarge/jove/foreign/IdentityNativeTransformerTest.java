package org.sarge.jove.foreign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

class IdentityNativeTransformerTest {
	private IdentityNativeTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		transformer = new IdentityNativeTransformer<>(ValueLayout.JAVA_INT);
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_INT, transformer.layout());
	}

	@Test
	void transform() {
		assertEquals(42, transformer.marshal(42, null));
	}

	@Test
	void returns() {
		assertEquals(42, transformer.unmarshal().apply(42));
	}
}
