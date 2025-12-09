package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.geometry.Ray.IntersectedSurface.EMPTY_INTERSECTIONS;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtility;

class SphereVolumeTest {
	private static final float RADIUS = 3;
	private static final float OUTSIDE = 4;

	private SphereVolume sphere;

	@BeforeEach
	void before() {
		sphere = new SphereVolume(Point.ORIGIN, RADIUS);
	}

	@Test
	@DisplayName("A sphere volume can be created that enclosed a given bounds")
	void of() {
		final Bounds bounds = new Bounds(new Point(-RADIUS, -RADIUS, -RADIUS), new Point(RADIUS, RADIUS, RADIUS));
		assertEquals(sphere, SphereVolume.of(bounds));
	}

	@Test
	void contains() {
		assertEquals(true, sphere.contains(Point.ORIGIN));
		assertEquals(true, sphere.contains(new Point(RADIUS, 0, 0)));
		assertEquals(false, sphere.contains(new Point(OUTSIDE, 0, 0)));
	}

	@Nested
	class SpherePlaneIntersectionTests {
		@Test
		void axes() {
			assertEquals(true, sphere.intersects(new Plane(X, 0)));
			assertEquals(true, sphere.intersects(new Plane(Y, 0)));
			assertEquals(true, sphere.intersects(new Plane(Z, 0)));
		}

		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new Plane(X, 1)));
			assertEquals(true, sphere.intersects(new Plane(X, -1)));
		}

		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new Plane(X, RADIUS)));
			assertEquals(true, sphere.intersects(new Plane(X, -RADIUS)));
		}

		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new Plane(X, OUTSIDE)));
			assertEquals(false, sphere.intersects(new Plane(X, -OUTSIDE)));
		}
	}

	@Nested
	class SphereSphereIntersectionTests {
		private Point outside;

		@BeforeEach
		void before() {
			outside = new Point(OUTSIDE, 0, 0);
		}

		@DisplayName("Sphere should always intersect itself")
		@Test
		void self() {
			assertEquals(true, sphere.intersects(sphere));
		}

		@DisplayName("Contained sphere intersects")
		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new SphereVolume(Point.ORIGIN, 1)));
		}

		@DisplayName("Overlapping spheres should intersect")
		@Test
		void intersecting() {
			assertEquals(true, sphere.intersects(new SphereVolume(outside, 2)));
		}

		@DisplayName("Touching spheres intersect")
		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new SphereVolume(outside, 1)));
		}

		@DisplayName("Non-intersecting spheres")
		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new SphereVolume(outside, MathsUtility.HALF)));
		}
	}

	@Nested
	class SphereExtentsIntersectionTests {
		@DisplayName("Extents contained by sphere intersects")
		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new Bounds(Point.ORIGIN, Point.ORIGIN)));
		}

		@DisplayName("Oberlapping extents and sphere intersects")
		@Test
		void intersects() {
			assertEquals(true, sphere.intersects(new Bounds(Point.ORIGIN, new Point(RADIUS, 0, 0))));
		}

		@DisplayName("Sphere touching a corner intersects")
		@Test
		void touching() {
			final Point pt = new Point(RADIUS, 0, 0);
			assertEquals(true, sphere.intersects(new Bounds(pt, pt)));
		}

		@DisplayName("Sphere outside the extents does not intersect")
		@Test
		void outside() {
			final Point pt = new Point(OUTSIDE, 0, 0);
			assertEquals(false, sphere.intersects(new Bounds(pt, pt)));
		}
	}

	@Nested
	class IntersectionTests {
		@Test
		void normal() {
			assertEquals(X.invert(), sphere.normal(new Point(OUTSIDE, 0, 0)));
			assertEquals(X, sphere.normal(new Point(-OUTSIDE, 0, 0)));
		}

		@DisplayName("A sphere behind the ray origin...")
		@Nested
		class Behind {
    		@DisplayName("does not intersect if the ray originates outside the sphere")
    		@Test
    		void outside() {
    			final Ray ray = new Ray(new Point(OUTSIDE, 0, 0), X);
    			assertEquals(EMPTY_INTERSECTIONS, sphere.intersections(ray));
    		}

    		@DisplayName("intersects if the ray originates inside the sphere")
    		@Test
    		void inside() {
    			final Ray ray = new Ray(new Point(1, 0, 0), X);
    			assertEquals(List.of(new Intersection(2, sphere)), sphere.intersections(ray));
    		}

    		@DisplayName("intersects if the ray originates on the sphere surface")
    		@Test
    		void touching() {
    			final Ray ray = new Ray(new Point(RADIUS, 0, 0), X);
    			assertEquals(List.of(new Intersection(0, sphere)), sphere.intersections(ray));
    		}
		}

		@DisplayName("A sphere ahead of the ray origin...")
		@Nested
		class Ahead {
    		@DisplayName("does not intersect if the ray originates outside of the sphere")
    		@Test
    		void outside() {
    			final Ray ray = new Ray(new Point(-OUTSIDE, +OUTSIDE, 0), X);
    			assertEquals(EMPTY_INTERSECTIONS, sphere.intersections(ray));
    		}

    		@DisplayName("intersects if the ray originates inside the sphere")
    		@Test
    		void inside() {
    			final Ray ray = new Ray(new Point(-1, 0, 0), X);
    			assertEquals(List.of(new Intersection(4, sphere)), sphere.intersections(ray));
    		}

    		@DisplayName("intersects twice if the ray crosses the sphere")
    		@Test
    		void intersects() {
    			final Ray ray = new Ray(new Point(-OUTSIDE, 0, 0), X);
    			assertEquals(List.of(new Intersection(1, sphere), new Intersection(7, sphere)), sphere.intersections(ray));
    		}

    		@DisplayName("intersects twice if the ray originates on the sphere surface")
    		@Test
    		void touching() {
    			final Ray ray = new Ray(new Point(-RADIUS, 0, 0), X);
    			assertEquals(List.of(new Intersection(0, sphere), new Intersection(6, sphere)), sphere.intersections(ray));
    		}
    	}
	}

	@Test
	void equals() {
		assertEquals(sphere, sphere);
		assertEquals(sphere, new SphereVolume(Point.ORIGIN, RADIUS));
		assertNotEquals(sphere, null);
		assertNotEquals(sphere, new SphereVolume(Point.ORIGIN, 1));
	}
}
