package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.*;

class DefaultNativeMapperTest {
	private DefaultNativeMapper<Integer, Integer> mapper;

	@BeforeEach
	void before() {
		mapper = new DefaultNativeMapper<>(int.class, JAVA_INT);
	}

	@Test
	void mapper() {
		assertEquals(int.class, mapper.type());
		assertEquals(JAVA_INT, mapper.layout());
	}

	@Test
	void toNative() {
		assertEquals(3, mapper.toNative(3, null));
	}

	@Test
	void toNativeNull() {
		assertEquals(MemorySegment.NULL, mapper.toNativeNull(null));
	}

	@Test
	void fromNative() {
		assertEquals(3, mapper.fromNative(3, null));
	}
}
