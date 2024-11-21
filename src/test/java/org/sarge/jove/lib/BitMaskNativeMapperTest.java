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
		assertEquals(ValueLayout.JAVA_INT, mapper.layout(null));
	}

	@Test
	void marshal() {
		assertEquals(3, mapper.marshal(mask, new NativeContext()));
	}

	@Test
	void marshalNull() {
		assertEquals(0, mapper.marshalNull(BitMask.class));
	}

	@Test
	void unmarshal() {
		assertEquals(mask, mapper.unmarshal(3, BitMask.class));
	}
}
