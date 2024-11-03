package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class KeyTableTest {
	private KeyTable table;

	@BeforeEach
	void before() {
		table = KeyTable.INSTANCE;
	}

	@Test
	void constructor() {
		assertNotNull(table);
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
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> table.code("COBBLERS"));
	}
}
