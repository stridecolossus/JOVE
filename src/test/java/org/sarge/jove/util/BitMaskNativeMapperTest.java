package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

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
		assertEquals(mapper, mapper.derive(null));
	}

	@Test
	void marshal() {
		assertEquals(3, mapper.marshal(mask, null));
	}

	@Test
	void marshalNull() {
		assertEquals(0, mapper.marshalNull());
	}

	@Test
	void returns() {
		assertEquals(mask, mapper.returns().apply(3));
	}

	@Test
	void reference() {
		assertThrows(UnsupportedOperationException.class, () -> mapper.reference());
	}
}
