package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtil;

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

	@DisplayName("The box factory positions particles randomly within the given bounds")
	@Test
	void box() {
		final var factory = PositionFactory.box(new Bounds(Point.ORIGIN, Point.ORIGIN), new Random());
		assertEquals(Point.ORIGIN, factory.position());
	}

	@DisplayName("The sphere factory positions particles randomly on the surface of a sphere")
	@Test
	void spherical() {
		final var vol = new SphereVolume(Point.ORIGIN, 3);
		final var factory = PositionFactory.sphere(vol, new Random());
		final Point pos = factory.position();
		assertTrue(MathsUtil.isEqual(3 * 3, pos.dot(pos)));
	}
}
