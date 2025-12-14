package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

class PrioritySelectorTest {
	private PrioritySelector<String> selector;

	@BeforeEach
	void before() {
		selector = new PrioritySelector<>("two"::equals, PrioritySelector.first());
	}

	@Test
	void select() {
		assertEquals("two", selector.select(List.of("one", "two")));
	}

	@Test
	void fallback() {
		assertEquals("one", selector.select(List.of("one", "one")));
	}

	@Test
	void empty() {
		assertThrows(NoSuchElementException.class, () -> selector.select(List.of()));
	}

	@Test
	void none() {
		selector = new PrioritySelector<>(_ -> false, _ -> null);
		assertThrows(NoSuchElementException.class, () -> selector.select(List.of()));
	}
}
