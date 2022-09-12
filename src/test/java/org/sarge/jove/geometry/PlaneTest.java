package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

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
		final Plane result = Plane.of(new Point(0, 1, 0), new Point(1, 1, 0), new Point(1, 1, -1));
		assertEquals(plane, result);
	}

	@Test
	void degenerate() {
		final Point p = Point.ORIGIN;
		assertThrows(IllegalArgumentException.class, () -> Plane.of(p, p, p));
	}

	@Test
	void distance() {
		assertEquals(-1, plane.distance(Point.ORIGIN));
		assertEquals(0, plane.distance(new Point(0, 1, 0)));
		assertEquals(1, plane.distance(new Point(0, 2, 0)));
		assertEquals(-2, plane.distance(new Point(0, -1, 0)));
	}

	@Test
	void normalize() {
//		assertEquals(plane, new Plane(new Vector(1, 0, 0), -1).normalize());
	}

	@Test
	void normalizeSelf() {
//		assertSame(plane, plane.normalize());
	}

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
			final Intersection intersection = plane.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{1}, intersection.distances());
			assertEquals(Y, intersection.normal(null));
		}

		@DisplayName("A ray touching the plane is intersecting")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(0, 1, 0), Y);
			final Intersection intersection = plane.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{-0f}, intersection.distances());
			assertEquals(Y, intersection.normal(null));
		}

		@DisplayName("A ray orthogonal to the plane is not intersecting")
		@Test
		void orthogonal() {
			assertEquals(Intersected.NONE, plane.intersection(new DefaultRay(Point.ORIGIN, X)));
			assertEquals(Intersected.NONE, plane.intersection(new DefaultRay(Point.ORIGIN, Z)));
		}

		@DisplayName("A ray pointing away from the plane is not intersecting")
		@Test
		void missing() {
			assertEquals(Intersected.NONE, plane.intersection(new DefaultRay(new Point(0, 2, 0), Y)));
			assertEquals(Intersected.NONE, plane.intersection(new DefaultRay(Point.ORIGIN, Y.invert())));
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
			assertEquals(Intersected.NONE, behind.intersection(new DefaultRay(new Point(0, 2, 0), Y.invert())));
		}

		@DisplayName("A ray behind the plane intersects even if it does not cross the plane")
		@Test
		void negative() {
			final Intersection intersection = behind.intersection(new DefaultRay(Point.ORIGIN, Y.invert()));
			assertEquals(false, intersection.isEmpty());
		}
	}

	@Nested
	class HalfSpaceIntersectionTests {
		private Intersected surface;

		@BeforeEach
		void before() {
			surface = plane.halfspace(HalfSpace.NEGATIVE);
			assertNotNull(surface);
		}

		@DisplayName("A ray that crosses the plane in front is not intersecting")
		@Test
		void positive() {
			assertEquals(Intersected.NONE, surface.intersection(new DefaultRay(new Point(0, 2, 0), Y.invert())));
		}

		@DisplayName("A ray behind the plane has undefined intersection results")
		@Test
		void behind() {
			final Intersection intersection = surface.intersection(new DefaultRay(Point.ORIGIN, Y));
			assertEquals(false, intersection.isEmpty());
			assertThrows(UnsupportedOperationException.class, () -> intersection.distances());
			assertThrows(UnsupportedOperationException.class, () -> intersection.normal(null));
		}

		@DisplayName("A ray behind the plane intersects even if it does not cross the plane")
		@Test
		void negative() {
			final Intersection intersection = surface.intersection(new DefaultRay(Point.ORIGIN, Y.invert()));
			assertEquals(false, intersection.isEmpty());
		}
	}
}
