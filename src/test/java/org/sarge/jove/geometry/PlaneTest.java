package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Vector.*;

import java.util.Iterator;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.*;

class PlaneTest {
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(X, -1);
	}

	@Test
	void constructor() {
		assertEquals(X, plane.normal());
		assertEquals(-1, plane.distance());
	}

	@Test
	void of() {
		assertEquals(plane, Plane.of(X, new Point(1, 0, 0)));
	}

	@Test
	void triangle() {
		final Plane result = Plane.of(new Point(1, 0, 0), new Point(1, 1, 0), new Point(1, 0, 1));
		assertEquals(plane, result);
	}

	@Test
	void distance() {
		assertEquals(-1, plane.distance(Point.ORIGIN));
		assertEquals(0, plane.distance(new Point(1, 0, 0)));
		assertEquals(1, plane.distance(new Point(2, 0, 0)));
	}

	@Test
	void normalize() {
		assertEquals(plane, new Plane(new Vector(1, 0, 0), -1).normalize());
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
			assertEquals(HalfSpace.POSITIVE, plane.halfspace(new Point(2, 0, 0)));
			assertEquals(HalfSpace.INTERSECT, plane.halfspace(new Point(1, 0, 0)));
			assertEquals(HalfSpace.NEGATIVE, plane.halfspace(Point.ORIGIN));
		}
	}

	@Nested
	class PlaneRayIntersectionTests {
		@DisplayName("A ray crossing the plane is intersecting")
		@Test
		void intersect() {
			final Ray ray = new DefaultRay(Point.ORIGIN, X);
			final Iterator<Intersection> itr = plane.intersections(ray);
			assertEquals(new Intersection(ray, 1, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray touching the plane is intersecting")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(1, 0, 0), X);
			final Iterator<Intersection> itr = plane.intersections(ray);
			assertEquals(new Intersection(ray, 0, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray orthogonal to the plane is not intersecting")
		@Test
		void orthogonal() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, Y)));
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, Z)));
		}

		@DisplayName("A ray parallel to the plane is intersecting")
		@Test
		void parallel() {
			final Ray ray = new DefaultRay(Point.ORIGIN, X);
			final Iterator<Intersection> itr = plane.intersections(ray);
			assertEquals(new Intersection(ray, 1, X), itr.next());
			assertEquals(false, itr.hasNext());
		}

		@DisplayName("A ray pointing away from the plane is not intersecting")
		@Test
		void above() {
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(new Point(2, 0, 0), X)));
			assertEquals(Intersected.NONE, plane.intersections(new DefaultRay(Point.ORIGIN, X.invert())));
		}
	}

	@Nested
	class RayBehindPlaneIntersectionTests {
		private Intersected behind;

		@BeforeEach
		void before() {
			behind = plane.behind();
			assertNotNull(behind);
		}

		@DisplayName("A ray that crosses the plane in front is not intersecting")
		@Test
		void behind() {
			assertEquals(Intersected.NONE, behind.intersections(new DefaultRay(new Point(2, 0, 0), X)));
		}
	}
}
