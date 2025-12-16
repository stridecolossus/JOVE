package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;

class KeyTableTest {
	private KeyTable table;

	@BeforeEach
	void before() {
		table = new KeyTable(Map.of(42, "key"));
	}

	@Test
	void name() {
		assertEquals("key", table.name(42));
	}

	@Test
	void unknown() {
		assertEquals("UNKNOWN", table.name(999));
	}

	@Test
	void code() {
		assertEquals(42, table.code("key"));
	}

	@Test
	void zero() {
		assertEquals(0, table.code("cobblers"));
	}

	@Test
	void map() {
		final Map<Integer, Button> buttons = table.map(Button::new);
		assertEquals(new Button(42, "key"), buttons.get(42));
	}

	@Test
	void instance() {
		final var instance = KeyTable.Instance.INSTANCE;
		instance.table(table);
		assertEquals(table, instance.table());
	}

	@Test
	void standard() {
		final KeyTable standard = KeyTable.defaultKeyTable();
		assertEquals(256, standard.code("ESCAPE"));
	}
}
