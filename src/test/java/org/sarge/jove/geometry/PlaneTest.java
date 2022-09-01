package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Point.ORIGIN;
import static org.sarge.jove.geometry.Vector.*;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.*;

class PlaneTest {
	private static final int DIST = 3; // Plane lies at (3,0,0) but note distance is -3

	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(X, -DIST);
	}

	@Test
	void constructor() {
		assertEquals(X, plane.normal());
		assertEquals(-DIST, plane.distance());
	}

	@Test
	void of() {
		assertEquals(plane, Plane.of(X, new Point(DIST, 0, 0)));
	}

	@Test
	void triangle() {
		final Plane result = Plane.of(new Point(DIST, 0, 0), new Point(DIST, 1, 0), new Point(DIST, 0, 1));
		assertEquals(plane, result);
	}

	@Test
	void distance() {
		assertEquals(-DIST, plane.distance(ORIGIN));
		assertEquals(0, plane.distance(new Point(DIST, 0, 0)));
		assertEquals(2 * -DIST, plane.distance(new Point(-DIST, 0, 0)));
		assertEquals(DIST, plane.distance(new Point(2 * DIST, 0, 0)));
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
	class HalfSpaceTests {
		@Test
		void of() {
			assertEquals(HalfSpace.INTERSECT, HalfSpace.of(0));
			assertEquals(HalfSpace.INTERSECT, HalfSpace.of(-0));
			assertEquals(HalfSpace.POSITIVE, HalfSpace.of(+1));
			assertEquals(HalfSpace.NEGATIVE, HalfSpace.of(-1));
		}

		@Test
		void halfspace() {
			assertEquals(HalfSpace.POSITIVE, plane.halfspace(new Point(4, 0, 0)));
			assertEquals(HalfSpace.NEGATIVE, plane.halfspace(ORIGIN));
			assertEquals(HalfSpace.INTERSECT, plane.halfspace(new Point(DIST, 0, 0)));
		}
	}

	@Nested
	class PlaneRayIntersectionTests {
		@Test
		void intersect() {
			final Ray ray = new DefaultRay(ORIGIN, X);
			final Iterator<Intersection> itr = plane.intersections(ray);
			assertEquals(new Intersection(ray, 3f, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(DIST, 0, 0), X);
			final Iterator<Intersection> itr = plane.intersections(ray);
			assertEquals(new Intersection(ray, 0, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@Test
		void miss() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(ORIGIN, Y)));
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(ORIGIN, Z)));
		}

		@Test
		void behind() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(new Point(4, 0, 0), X)));
		}
	}
}
