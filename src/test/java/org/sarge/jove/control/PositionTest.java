package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PositionTest {
	private Position pos;

	@BeforeEach
	void before() {
		pos = new Position(1, 2);
	}

	@Test
	void constructor() {
		assertEquals(1, pos.x());
		assertEquals(2, pos.y());
		assertEquals(Position.TYPE, pos.type());
	}

	@Test
	void equals() {
		assertEquals(true, pos.equals(pos));
		assertEquals(true, pos.equals(new Position(1, 2)));
		assertEquals(false, pos.equals(null));
		assertEquals(false, pos.equals(new Position(3, 4)));
	}

	@Test
	void type() {
		assertEquals("Position", Position.TYPE.name());
		assertEquals(Position.TYPE.name().hashCode(), Position.TYPE.hashCode());
	}

	@Test
	void parse() {
		assertEquals(Position.TYPE, Position.TYPE.parse(null));
	}
}
