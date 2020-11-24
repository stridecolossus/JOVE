package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelectorTest {
	private Selector<Integer> selector;

	@BeforeEach
	void before() {
		selector = new Selector<>(num -> num == 1, 2);
	}

	@Test
	void select() {
		assertEquals(Integer.valueOf(1), selector.select(List.of(1)));
		assertEquals(Integer.valueOf(1), selector.select(List.of(2, 1)));
	}

	@Test
	void selectDefault() {
		assertEquals(Integer.valueOf(2), selector.select(List.of()));
		assertEquals(Integer.valueOf(2), selector.select(List.of(2)));
	}

	@Test
	void selectEmptyDefault() {
		selector = new Selector<>(num -> num == 1, null);
		assertThrows(NoSuchElementException.class, () -> selector.select(List.of()));
	}
}
