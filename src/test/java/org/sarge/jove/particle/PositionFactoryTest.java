package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class PositionFactoryTest {
	@Test
	public void origin() {
		assertEquals(Point.ORIGIN, PositionFactory.ORIGIN.position());
	}

	@Test
	public void point() {
		final Point pt = new Point(1, 2, 3);
		final PositionFactory factory = PositionFactory.of(pt);
		assertNotNull(factory);
		assertEquals(pt, factory.position());
	}
}
