package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class PositionTest {
	private Position event;

	@BeforeEach
	void before() {
		event = new Position(2, 3);
	}

	@Test
	void constructor() {
		assertEquals(2, event.x());
		assertEquals(3, event.y());
	}

	@Test
	void matches() {
		assertEquals(true, event.matches(event));
	}
}
