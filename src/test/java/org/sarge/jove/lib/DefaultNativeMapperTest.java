package org.sarge.jove.lib;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.ValueLayout;

import org.junit.jupiter.api.*;

class DefaultNativeMapperTest {
	private DefaultNativeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new DefaultNativeMapper(boolean.class, ValueLayout.JAVA_BOOLEAN);
	}

	@Test
	void type() {
		assertEquals(boolean.class, mapper.type());
	}

	@Test
	void layout() {
		assertEquals(ValueLayout.JAVA_BOOLEAN, mapper.layout());
	}

	@Test
	void equals() {
		assertEquals(mapper, mapper);
		assertEquals(mapper, new DefaultNativeMapper(boolean.class, ValueLayout.JAVA_BOOLEAN));
		assertNotEquals(mapper, null);
		assertNotEquals(mapper, new DefaultNativeMapper(int.class, ValueLayout.JAVA_INT));
	}
}
