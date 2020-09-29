package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CoordinateTest {
	private Coordinate coords;

	@BeforeEach
	void before() {
		coords = new Coordinate(1, 2);
	}

	@Test
	void constructor() {
		assertEquals(1, coords.x());
		assertEquals(2, coords.y());
	}

	@Test
	void origin() {
		assertEquals(new Coordinate(0, 0), Coordinate.ORIGIN);
	}

	@Test
	void equals() {
		assertEquals(true, coords.equals(coords));
		assertEquals(false, coords.equals(null));
		assertEquals(false, coords.equals(new Coordinate(3, 4)));
	}
}
