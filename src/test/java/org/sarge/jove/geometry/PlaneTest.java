package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.*;

class PlaneTest {
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(Y, -1);
	}

	@Test
	void constructor() {
		assertEquals(Y, plane.normal());
		assertEquals(-1, plane.distance());
	}

	@Test
	void of() {
		assertEquals(plane, Plane.of(Y, new Point(0, 1, 0)));
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
		assertThrows(IllegalArgumentException.class, () -> Plane.of(new Triangle(Point.ORIGIN, Point.ORIGIN, Point.ORIGIN)));
	}

	@Test
	void distance() {
		assertEquals(-1, plane.distance(Point.ORIGIN));
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
			final Ray ray = new DefaultRay(Point.ORIGIN, Y);
			final Iterator<Intersection> results = plane.intersections(ray).iterator();
			final Intersection intersection = results.next();
			assertEquals(1f, intersection.distance());
			assertEquals(Y, intersection.normal());
			assertEquals(false, results.hasNext());
		}

		@DisplayName("A ray touching the plane is intersecting")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(0, 1, 0), Y);
			final Iterator<Intersection> results = plane.intersections(ray).iterator();
			final Intersection intersection = results.next();
			assertEquals(-0f, intersection.distance());
			assertEquals(Y, intersection.normal());
			assertEquals(false, results.hasNext());
		}

		@DisplayName("A ray orthogonal to the plane is not intersecting")
		@Test
		void orthogonal() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, X)));
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, Z)));
		}

		@DisplayName("A ray pointing away from the plane is not intersecting")
		@Test
		void missing() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(new Point(0, 2, 0), Y)));
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, Y.invert())));
		}
	}

	@Nested
	class BehindIntersectionTests {
		private Intersected behind;

		@BeforeEach
		void before() {
			behind = plane.behind();
			assertNotNull(behind);
		}

		@DisplayName("A ray that crosses the plane in front is not intersecting")
		@Test
		void behind() {
			assertEquals(Intersected.NONE, behind.intersections(new DefaultRay(new Point(0, 2, 0), Y.invert())));
		}

		@DisplayName("A ray behind the plane intersects even if it does not cross the plane")
		@Test
		void negative() {
			final Ray ray = new DefaultRay(Point.ORIGIN, Y.invert());
			final Iterator<Intersection> results = behind.intersections(ray).iterator();
			assertEquals(true, results.hasNext());
		}
	}

	@Nested
	class HalfSpaceIntersectionTests {
		private Intersected surface;

		@BeforeEach
		void before() {
			surface = plane.halfspace(HalfSpace.NEGATIVE);
		}

		@DisplayName("A ray that crosses the plane in front is not intersecting")
		@Test
		void positive() {
			final Ray ray = new DefaultRay(new Point(0, 2, 0), Y.invert());
			assertEquals(Intersected.NONE, surface.intersections(ray));
		}

		@DisplayName("A ray behind the plane has undefined intersection results")
		@Test
		void behind() {
			final Ray ray = new DefaultRay(Point.ORIGIN, Y);
			final Iterator<Intersection> results = surface.intersections(ray).iterator();
			assertEquals(true, results.hasNext());
		}

		@DisplayName("A ray behind the plane intersects even if it does not cross the plane")
		@Test
		void negative() {
			final Ray ray = new DefaultRay(Point.ORIGIN, Y.invert());
			final Iterator<Intersection> results = surface.intersections(ray).iterator();
			assertEquals(true, results.hasNext());
		}
	}
}
