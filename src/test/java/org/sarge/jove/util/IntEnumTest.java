package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

class IntEnumTest {
	private ReverseMapping<MockEnum> mapping;

	@BeforeEach
	void before() {
		mapping = IntEnum.reverse(MockEnum.class);
	}

	@Test
	void constructor() {
		assertNotNull(mapping);
	}

	@DisplayName("A native value can be mapped to a valid enumeration constant")
	@Test
	void map() {
		assertEquals(MockEnum.A, mapping.map(1));
		assertEquals(MockEnum.B, mapping.map(2));
		assertEquals(MockEnum.C, mapping.map(4));
	}

	@DisplayName("An invalid native value cannot be mapped to an enumeration constant")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> mapping.map(0));
		assertThrows(IllegalArgumentException.class, () -> mapping.map(999));
	}
}
