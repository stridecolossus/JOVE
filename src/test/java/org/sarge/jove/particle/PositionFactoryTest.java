package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class PositionFactoryTest {
	@DisplayName("The origin factory positions particles at the origin")
	@Test
	void origin() {
		assertEquals(Point.ORIGIN, PositionFactory.ORIGIN.position());
	}

	@DisplayName("The literal factory positions particles at the given point")
	@Test
	void point() {
		final Point pt = new Point(1, 2, 3);
		final PositionFactory factory = PositionFactory.of(pt);
		assertEquals(pt, factory.position());
	}
}
