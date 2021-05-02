package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.sarge.jove.geometry.Point.ORIGIN;
import static org.sarge.jove.geometry.Vector.X_AXIS;
import static org.sarge.jove.geometry.Vector.Y_AXIS;
import static org.sarge.jove.geometry.Vector.Z_AXIS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.Intersection;

class PlaneTest {
	private static final int DIST = 3; // Plane lies at (3,0,0) but note distance is -3

	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(X_AXIS, -DIST);
	}

	@Test
	void constructor() {
		assertEquals(X_AXIS, plane.normal());
		assertEquals(-DIST, plane.distance());
	}

	@Test
	void distance() {
		assertEquals(-DIST, plane.distance(ORIGIN));
		assertEquals(0, plane.distance(new Point(DIST, 0, 0)));
	}

	@Test
	void space() {
		assertEquals(HalfSpace.POSITIVE, plane.space(new Point(4, 0, 0)));
		assertEquals(HalfSpace.NEGATIVE, plane.space(ORIGIN));
		assertEquals(HalfSpace.INTERSECT, plane.space(new Point(DIST, 0, 0)));
	}

	@Test
	void triangle() {
		final Plane result = Plane.of(new Point(DIST, 0, 0), new Point(DIST, 1, 0), new Point(DIST, 0, 1));
		assertEquals(plane, result);
	}

	@Test
	void of() {
		assertEquals(plane, Plane.of(X_AXIS, new Point(DIST, 0, 0)));
	}

	@Test
	void normalize() {
		assertEquals(plane, new Plane(new Vector(DIST, 0, 0), -DIST * DIST).normalize());
	}

	@Test
	void normalizeSelf() {
		assertSame(plane, plane.normalize());
	}

	@Nested
	class PlaneRayIntersectionTests {
		@Test
		void intersect() {
			assertEquals(Intersection.of(3f),  plane.intersect(new Ray(ORIGIN, X_AXIS)));
			assertEquals(Intersection.of(-0f), plane.intersect(new Ray(new Point(DIST, 0, 0), X_AXIS)));
			assertEquals(Intersection.of(+0f), plane.intersect(new Ray(new Point(DIST, 0, 0), X_AXIS.negate())));
		}

		@Test
		void miss() {
			assertEquals(Intersection.NONE, plane.intersect(new Ray(ORIGIN, Y_AXIS)));
			assertEquals(Intersection.NONE, plane.intersect(new Ray(ORIGIN, Z_AXIS)));
		}

		@Test
		void behind() {
			assertEquals(Intersection.NONE, plane.intersect(new Ray(new Point(4, 0, 0), X_AXIS)));
		}
	}
}
