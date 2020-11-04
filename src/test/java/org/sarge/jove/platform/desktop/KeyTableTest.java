package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KeyTableTest {
	private static final int CODE = 'A';
	private static final String NAME = "A";

	private KeyTable table;

	@BeforeEach
	void before() {
		table = new KeyTable();
	}

	@Test
	void containsKeyCode() {
		assertEquals(true, table.contains(CODE));
	}

	@Test
	void containsKeyCodeUnknown() {
		assertEquals(false, table.contains(999));
	}

	@Test
	void name() {
		assertEquals(NAME, table.name(CODE));
	}

	@Test
	void nameUnknownKey() {
		assertThrows(IllegalArgumentException.class, () -> table.name(999));
	}

	@Test
	void containsKeyName() {
		assertEquals(true, table.contains(NAME));
	}

	@Test
	void containsKeyNameUnknown() {
		assertEquals(false, table.contains("cobblers"));
	}

	@Test
	void code() {
		assertEquals(CODE, table.code(NAME));
	}

	@Test
	void codeUnknownKey() {
		assertThrows(IllegalArgumentException.class, () -> table.code("cobblers"));
	}

	@Test
	void unknown() {
		assertEquals(false, table.contains(0));
	}

	@Test
	void map() {
		final var map = table.map();
		assertNotNull(map);
		assertEquals(NAME, map.get(CODE));
	}
}
