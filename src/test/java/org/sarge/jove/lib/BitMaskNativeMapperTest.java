package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.BitMask;

class BitMaskNativeMapperTest {
	private BitMaskNativeMapper mapper;
	private BitMask<?> mask;

	@BeforeEach
	void before() {
		mapper = new BitMaskNativeMapper();
		mask = new BitMask<>(3);
	}

	@Test
	void mapper() {
		assertEquals(BitMask.class, mapper.type());
		assertEquals(ValueLayout.JAVA_INT, mapper.layout());
	}

	@Test
	void toNative() {
		assertEquals(3, mapper.toNative(mask, new NativeContext()));
	}

	@Test
	void toNativeNull() {
		assertEquals(0, mapper.toNativeNull(null));
	}

	@Test
	void fromNative() {
		assertEquals(mask, mapper.fromNative(3, null));
	}
}
