package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

class KeyTableTest {
	private KeyTable table;

	@BeforeEach
	void before() {
		table = new KeyTable();
	}

	@Test
	void name() {
		assertEquals("ESCAPE", table.name(256));
	}

	@Test
	void unknown() {
		assertEquals("UNKNOWN", table.name(999));
	}

	@Test
	void code() {
		assertEquals(256, table.code("ESCAPE"));
	}

	@Test
	void zero() {
		assertEquals(0, table.code("COBBLERS"));
	}
}
