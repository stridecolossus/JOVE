package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.geometry.Vector;

class BoundingBoxTest {
	private BoundingBox box;
	private Bounds bounds;

	@BeforeEach
	void before() {
		bounds = new Bounds(new Point(1, 2, 3), new Point(4, 5, 6));
		box = new BoundingBox(bounds);
	}

	@DisplayName("A bounding box is an adapter for min/max bounds")
	@Test
	void constructor() {
		assertEquals(bounds, box.bounds());
	}

	@Test
	void equals() {
		assertEquals(box, box);
		assertEquals(box, new BoundingBox(bounds));
		assertNotEquals(box, null);
		assertNotEquals(box, new BoundingBox(Bounds.EMPTY));
	}

	@DisplayName("A bounding box can determine whether a given point lies within the bounds")
	@Test
	void contains() {
		assertTrue(box.contains(new Point(1, 2, 3)));
		assertTrue(box.contains(new Point(2, 3, 4)));
		assertTrue(box.contains(new Point(4, 5, 6)));
		assertFalse(box.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
		assertEquals(false, box.intersects(new EmptyVolume()));
	}

	@Nested
	class BoxSphereIntersectionTests {
		@DisplayName("A bounding box intersects a sphere that it encloses")
		@Test
		void intersects() {
			assertEquals(true, box.intersects(new SphereVolume(new Sphere(bounds.centre(), 1))));
			assertEquals(true, box.intersects(new SphereVolume(new Sphere(bounds.centre(), 4))));
		}

		@DisplayName("A bounding box is intersected by a sphere that encloses its bounds")
		@Test
		void enclosed() {
			assertEquals(true, box.intersects(new SphereVolume(new Sphere(Point.ORIGIN, Float.MAX_VALUE))));
		}

		@DisplayName("A bounding box intersects a sphere that is touching the box")
		@Test
		void touching() {
			assertEquals(true, box.intersects(new SphereVolume(new Sphere(new Point(1, 2, 2), 1))));
		}

		@DisplayName("A bounding box does not intersect a sphere that is outside of its bounds")
		@Test
		void outside() {
			assertEquals(false, box.intersects(new SphereVolume(new Sphere(Point.ORIGIN, 3))));
		}
	}

	@Nested
	class BoxBoxIntersectionTests {
		@DisplayName("A bounding box intersects itself")
		@Test
		void self() {
			assertEquals(true, box.intersects(box));
		}

		@DisplayName("A bounding box intersects another box that is encloses")
		@Test
		void contains() {
			assertEquals(true, box.intersects(new BoundingBox(new Bounds(bounds.centre(), bounds.centre()))));
		}

		@DisplayName("A bounding box intersects another overl")
		@Test
		void overlapping() {
			assertEquals(true, box.intersects(new BoundingBox(new Bounds(Point.ORIGIN, bounds.min()))));
		}

		@DisplayName("A bounding box does not intersect another box that is outside of its bounds")
		@Test
		void outside() {
			assertEquals(false, box.intersects(new BoundingBox(new Bounds(Point.ORIGIN, Point.ORIGIN))));
		}
	}

	@Nested
	class BoxRayIntersectionTests {
		@BeforeEach
		void before() {
			bounds = new Bounds(new Point(1, 2, 0), new Point(3, 4, 0));
			box = new BoundingBox(bounds);
		}

		@DisplayName("A ray crossing a bounding box has two intersections")
		@Test
		void intersect() {
			final Ray ray = new DefaultRay(new Point(0, 2, 0), new Vector(1, 1, 0).normalize());
			final float dist = (float) Math.sqrt(2);
			final Intersection a = Intersection.of(ray, dist, X.invert());
			final Intersection b = Intersection.of(ray, 2 * dist, Y);
			assertEquals(List.of(a, b), box.intersections(ray));
		}

		@DisplayName("A ray has a single intersection with a bounding box that it is touching")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(3, 3, 0), X);
			final Intersection expected = Intersection.of(ray, 0, X);
			assertEquals(List.of(expected), box.intersections(ray));
		}

		@DisplayName("A ray has a single intersection if it inside the box")
		@Test
		void inside() {
			final Ray ray = new DefaultRay(new Point(2, 3, 0), X);
			final Intersection expected = Intersection.of(ray, 1, X);
			assertEquals(List.of(expected), box.intersections(ray));
		}

		@DisplayName("A ray can intersect a corner of a bounding box")
		@Test
		void corner() {
			final Ray ray = new DefaultRay(new Point(0, 3, 0), new Vector(1, 1, 0).normalize());
			final Iterator<Intersection> results = box.intersections(ray).iterator();
			final Intersection intersection = results.next();
			assertEquals(Math.sqrt(2), intersection.distance(), 0.001f);
			assertEquals(new Point(1, 4, 0), intersection.point());
			assertEquals(false, results.hasNext());
		}

		@DisplayName("A ray that points away from the box has no intersections")
		@Test
		void miss() {
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(0, 3, 0), X.invert())));
		}

		@DisplayName("A ray does not intersect a bounding box if it is behind the ray origin")
		@Test
		void behind() {
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(4, 3, 0), X)));
		}

		@DisplayName("A ray does not intersect a bounding box if the direction is parallel to that box")
		@Test
		void parallel() {
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(0, 1, 0), X)));
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(0, 5, 0), X)));
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(0, 0, 0), Y)));
			assertEquals(Intersected.NONE, box.intersections(new DefaultRay(new Point(4, 0, 0), Y)));
		}
	}

	@Nested
	class BoxPlaneIntersectionTests {
		@Test
		void intersects() {
			final Point centre = bounds.centre();
			assertEquals(true, box.intersects(new Plane(Axis.Y, centre)));
			assertEquals(true, box.intersects(new Plane(Axis.Y.invert(), centre)));
		}
	}
}
