package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.InputEvent.Type.Parser;

public class PositionTest {
	private static final String NAME = "pos";

	private Position pos;

	@BeforeEach
	void before() {
		pos = new Position(NAME);
	}

	@Test
	void constructor() {
		assertEquals(NAME, pos.name());
	}

	@Test
	void parse() throws Exception {
		assertEquals(pos, Position.parse(NAME));
	}

	@Test
	void parser() throws Exception {
		final Parser parser = new Parser();
		final var result = parser.parse(Position.class.getName() + " " + NAME);
		assertEquals(pos, result);
	}

	@Test
	void equals() {
		assertEquals(true, pos.equals(pos));
		assertEquals(true, pos.equals(new Position(NAME)));
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
