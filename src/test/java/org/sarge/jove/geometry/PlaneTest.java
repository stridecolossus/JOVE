package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.geometry.Point.ORIGIN;
import static org.sarge.jove.geometry.Ray.IntersectedSurface.EMPTY_INTERSECTIONS;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.Intersection;

class PlaneTest {
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(Axis.Y, -1);
	}

	@Test
	void constructor() {
		assertEquals(Axis.Y, plane.normal());
		assertEquals(-1, plane.distance());
	}

	@Test
	void point() {
		assertEquals(plane, new Plane(Axis.Y, new Point(0, 1, 0)));
	}

	@Test
	void triangle() {
		final Point a = new Point(0, 1, 0);
		final Point b = new Point(1, 1, 0);
		final Point c = new Point(1, 1, -1);
		assertEquals(plane, Plane.of(new Triangle(a, b, c)));
	}

	@Test
	void degenerate() {
		assertThrows(IllegalArgumentException.class, () -> Plane.of(new Triangle(ORIGIN, ORIGIN, ORIGIN)));
	}

	@Test
	void distance() {
		assertEquals(-1, plane.distance(ORIGIN));
		assertEquals(0, plane.distance(new Point(0, 1, 0)));
		assertEquals(1, plane.distance(new Point(0, 2, 0)));
		assertEquals(-2, plane.distance(new Point(0, -1, 0)));
	}

// TODO
//	@Test
//	void normalize() {
////		assertEquals(plane, new Plane(new Vector(1, 0, 0), -1).normalize());
//	}
//
//	@Test
//	void normalizeSelf() {
////		assertSame(plane, plane.normalize());
//	}

	@Nested
	class HalfSpaceTests {
		@Test
		void of() {
			assertEquals(HalfSpace.INTERSECT, HalfSpace.of(0));
			assertEquals(HalfSpace.INTERSECT, HalfSpace.of(-0));
			assertEquals(HalfSpace.POSITIVE,  HalfSpace.of(+1));
			assertEquals(HalfSpace.NEGATIVE,  HalfSpace.of(-1));
		}

		@Test
		void halfspace() {
			assertEquals(HalfSpace.POSITIVE,  plane.halfspace(new Point(0, 2, 0)));
			assertEquals(HalfSpace.INTERSECT, plane.halfspace(new Point(0, 1, 0)));
			assertEquals(HalfSpace.NEGATIVE,  plane.halfspace(new Point(0, 0, 0)));
		}
	}

	@Nested
	class IntersectionTests {
		@DisplayName("A ray crossing the plane is intersecting")
		@Test
		void intersect() {
			final Ray ray = new Ray(Point.ORIGIN, Axis.Y);
			final Iterator<Intersection> results = plane.intersections(ray).iterator();
			final Intersection intersection = results.next();
			assertEquals(1f, intersection.distance());
			assertEquals(false, results.hasNext());
		}

		@DisplayName("A ray touching the plane is intersecting")
		@Test
		void touching() {
			final Ray ray = new Ray(new Point(0, 1, 0), Axis.Y);
			final Iterator<Intersection> results = plane.intersections(ray).iterator();
			final Intersection intersection = results.next();
			assertEquals(-0f, intersection.distance());
			assertEquals(false, results.hasNext());
		}

		@DisplayName("A ray orthogonal to the plane is not intersecting")
		@Test
		void orthogonal() {
			assertEquals(EMPTY_INTERSECTIONS, plane.intersections(new Ray(Point.ORIGIN, X)));
			assertEquals(EMPTY_INTERSECTIONS, plane.intersections(new Ray(Point.ORIGIN, Z)));
		}

		@DisplayName("A ray pointing away from the plane is not intersecting")
		@Test
		void missing() {
			assertEquals(EMPTY_INTERSECTIONS, plane.intersections(new Ray(new Point(0, 2, 0), Axis.Y)));
			assertEquals(EMPTY_INTERSECTIONS, plane.intersections(new Ray(Point.ORIGIN, Axis.Y.invert())));
		}

		@Test
		void normal() {
			assertEquals(Axis.Y, plane.normal(null));
		}
	}
}
