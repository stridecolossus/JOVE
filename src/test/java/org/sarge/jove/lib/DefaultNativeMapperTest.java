package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class DefaultNativeMapperTest {
	private DefaultNativeMapper<Integer> mapper;
	private Arena arena;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		mapper = new DefaultNativeMapper<>(int.class, ValueLayout.JAVA_INT);
	}

	@Test
	void constructor() {
		assertEquals(int.class, mapper.type());
		assertEquals(ValueLayout.JAVA_INT, mapper.layout());
	}

	@Test
	void toNative() {
		assertEquals(3, mapper.toNative(3, arena));
	}

	@Test
	void toNativeNull() {
		assertThrows(UnsupportedOperationException.class, () -> mapper.toNativeNull(Integer.class));
	}
}
