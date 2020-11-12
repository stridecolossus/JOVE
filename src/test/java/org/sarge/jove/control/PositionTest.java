package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PositionTest {
	private Position pos;

	@BeforeEach
	void before() {
		pos = new Position("pos");
	}

	@Test
	void constructor() {
		assertEquals("pos", pos.name());
	}

	@Test
	void equals() {
		assertEquals(true, pos.equals(pos));
		assertEquals(true, pos.equals(new Position("pos")));
		assertEquals(false, pos.equals(null));
		assertEquals(false, pos.equals(new Position("other")));
	}

	@Nested
	class EventTests {
		private Position.Event event;

		@BeforeEach
		void before() {
			event = new Position.Event(pos, 1, 2);
		}

		@Test
		void constructor() {
			assertEquals(pos, event.type());
			assertEquals(1, event.x());
			assertEquals(2, event.y());
		}
	}
}
