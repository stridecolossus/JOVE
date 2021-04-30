package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

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

	@Test
	public void extents() {
		final var factory = PositionFactory.extents(new Extents(Point.ORIGIN, Point.ORIGIN), new Random());
		assertEquals(Point.ORIGIN, factory.position());
	}

	@Disabled
	@Test
	public void spherical() {
		final Point pos = PositionFactory.sphere(3, new Random()).position();
		assertNotNull(pos);
		assertEquals(3 * 3, new Vector(pos).magnitude());
	}
}
