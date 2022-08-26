package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

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

	@Nested
	class IntersectionTests {
		@Test
		void none() {
			assertEquals(false, Intersection.NONE.hasNext());
		}

		@Test
		void constructor() {
			final Intersection intersection = new Intersection(3, Vector.X);
			assertEquals(3, intersection.distance());
			assertEquals(Vector.X, intersection.normal());
		}

		@Test
		void iterator() {
			final Intersection intersection = new Intersection(3, Vector.X);
			final Iterator<Intersection> itr = Intersection.iterator(() -> List.of(intersection, intersection));
			assertEquals(true, itr.hasNext());
			assertEquals(intersection, itr.next());
			assertEquals(intersection, itr.next());
			assertEquals(false, itr.hasNext());
		}
	}
}
