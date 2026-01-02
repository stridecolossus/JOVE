package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;

class KeyTableTest {
	private KeyTable table;
	private Button key;

	@BeforeEach
	void before() {
		key = new Button(42, "key");
		table = new KeyTable(List.of(key));
	}

	@Test
	void keys() {
		assertEquals(key, table.keys().get("key"));
	}

	@Test
	void index() {
		assertEquals(key, table.index().get(42));
	}

	@Test
	void instance() {
		KeyTable.Instance.INSTANCE.set(table);
		assertEquals(table, KeyTable.Instance.INSTANCE.get());
	}

	@Test
	void defaultKeyTable() {
		final KeyTable def = KeyTable.Instance.INSTANCE.get();
		assertEquals(new Button(256, "ESCAPE"), def.index().get(256));
	}
}
