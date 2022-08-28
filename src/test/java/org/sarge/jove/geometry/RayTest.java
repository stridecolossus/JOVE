package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;

class RayTest {
	private DefaultRay ray;

	@BeforeEach
	void before() {
		ray = new DefaultRay(Point.ORIGIN, Vector.X);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Vector.X, ray.direction());
	}

	@Test
	void point() {
		assertEquals(Point.ORIGIN, ray.point(0));
		assertEquals(new Point(1, 0, 0), ray.point(1));
		assertEquals(new Point(2, 0, 0), ray.point(2));
	}

	@Test
	void none() {
		assertEquals(false, Intersection.NONE.hasNext());
	}

	@Test
	void intersection() {
		final Intersection intersection = Intersection.of(3, Vector.Y);
		assertEquals(3, intersection.distance());
		assertEquals(Vector.Y, intersection.normal());
		assertEquals(intersection, intersection);
		assertEquals(intersection, Intersection.of(3, Vector.Y));
		assertNotEquals(intersection, null);
		assertNotEquals(intersection, mock(Intersection.class));
	}
}
