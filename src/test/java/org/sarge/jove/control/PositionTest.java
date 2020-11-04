package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PositionTest {
	private Position.Event event;

	@BeforeEach
	void before() {
		event = new Position.Event(1, 2);
	}

	@Test
	void constructor() {
		assertEquals(1, event.x());
		assertEquals(2, event.y());
		assertEquals(Position.TYPE, event.type());
	}

	@Test
	void equals() {
		assertEquals(true, event.equals(event));
		assertEquals(true, event.equals(new Position.Event(1, 2)));
		assertEquals(false, event.equals(null));
		assertEquals(false, event.equals(new Position.Event(3, 4)));
	}

	@Test
	void parse() {
		assertEquals(Position.TYPE, Position.parse("Position"));
	}
}
