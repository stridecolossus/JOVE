package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Button;

class KeyTableTest {
	private KeyTable table;

	@BeforeEach
	void before() {
		table = KeyTable.defaultKeyTable();
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

	@Test
	void map() {
		final Map<Integer, Button> buttons = table.map(Button::new);
		assertEquals(new Button(256, "ESCAPE"), buttons.get(256));
	}
}
