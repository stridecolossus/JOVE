package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

class NativeBooleanConverterTest {
	private NativeBooleanConverter converter;

	@BeforeEach
	void before() {
		converter = new NativeBooleanConverter();
	}

	@Test
	void nativeType() {
		assertEquals(Integer.class, converter.nativeType());
	}

	@Test
	void fromNative() {
		assertEquals(true, converter.fromNative(1, null));
		assertEquals(true, converter.fromNative(2, null));
		assertEquals(true, converter.fromNative(-1, null));
		assertEquals(false, converter.fromNative(0, null));
		assertEquals(false, converter.fromNative(null, null));
	}

	@Test
	void toNative() {
		assertEquals(1, converter.toNative(true, null));
		assertEquals(0, converter.toNative(false, null));
		assertEquals(0, converter.toNative(null, null));
	}
}
