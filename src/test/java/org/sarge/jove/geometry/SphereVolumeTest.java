package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.geometry.Vector.*;
import static org.sarge.jove.util.MathsUtil.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtil;

class SphereVolumeTest {
	private static final float RADIUS = 3;
	private static final float OUTSIDE = 4;

	private SphereVolume sphere;

	@BeforeEach
	void before() {
		sphere = new SphereVolume(Point.ORIGIN, RADIUS);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, sphere.centre());
		assertEquals(3f, sphere.radius());
	}

	@Test
	@DisplayName("Create a sphere containing the given bounds")
	void of() {
		sphere = SphereVolume.of(new Bounds(new Point(1, 2, 3), new Point(5, 6, 7)));
		assertNotNull(sphere);
		assertEquals(new Point(3, 4, 5), sphere.centre());
		assertEquals(2f, sphere.radius());
	}

	@Test
	void contains() {
		assertEquals(true, sphere.contains(Point.ORIGIN));
		assertEquals(true, sphere.contains(new Point(RADIUS, 0, 0)));
		assertEquals(false, sphere.contains(new Point(OUTSIDE, 0, 0)));
	}

	@DisplayName("Intersection test should delegate to sphere-specific method")
	@Test
	void intersectDelegate() {
		final Volume vol = mock(Volume.class);
		assertEquals(false, sphere.intersects(vol));
		verify(vol).intersects(sphere);
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
			assertEquals(false, sphere.intersects(new SphereVolume(outside, MathsUtil.HALF)));
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
	class SphereRayIntersectionTests {
		/**
		 * @param origin			Ray origin
		 * @param expected			Expected intersection points (X axis only, relative to ray origin)
		 */
		private void test(Point origin, float... expected) {
//			// Calc expected distances relative to ray origin
//			final var list = Arrays.stream(expected).map(n -> n - origin.x).toList();

			// Check intersection results
			final Intersection result = sphere.intersect(new Ray(origin, X));
			assertNotNull(result);
			assertArrayEquals(expected, result.distances());
		}

		@DisplayName("Sphere behind, does not intersect")
		@Test
		void behindNotIntersecting() {
			test(new Point(OUTSIDE, 0, 0));
		}

		@DisplayName("Sphere behind, ray originates inside the sphere")
		@Test
		void behindInside() {
			test(new Point(1, 0, 0), 2);
		}

		@DisplayName("Sphere behind, ray originates on the sphere surface")
		@Test
		void behindTouching() {
			test(new Point(RADIUS, 0, 0), 0);
		}

		@DisplayName("Sphere ahead, does not intersect")
		@Test
		void outside() {
			test(new Point(-OUTSIDE, OUTSIDE, 0));
		}

		@DisplayName("Sphere ahead, ray originates inside the sphere")
		@Test
		void inside() {
			test(new Point(-1, 0, 0), 4);
		}

		@DisplayName("Sphere ahead, intersects twice")
		@Test
		void intersects() {
			test(new Point(-OUTSIDE, 0, 0), 1, 7);
		}

		@DisplayName("Sphere ahead, ray originates on the sphere surface")
		@Test
		void touching() {
			test(new Point(-RADIUS, 0, 0), 0, 6);
		}
	}

	@Nested
	class UnitSphereTests {
		@Test
		void top() {
			assertEquals(new Vector(0, -1, 0), SphereVolume.vector(0, -HALF_PI));
			assertEquals(new Vector(0, -1, 0), SphereVolume.vector(TWO_PI, -HALF_PI));
		}

		@Test
		void bottom() {
			assertEquals(new Vector(0, 1, 0), SphereVolume.vector(0, +HALF_PI));
			assertEquals(new Vector(0, 1, 0), SphereVolume.vector(TWO_PI, +HALF_PI));
		}

		@Test
		void middle() {
			assertEquals(new Vector(0, 0, 1), SphereVolume.vector(PI, 0));
		}
	}

	@Test
	void equals() {
		assertEquals(true, sphere.equals(sphere));
		assertEquals(true, sphere.equals(new SphereVolume(Point.ORIGIN, RADIUS)));
		assertEquals(false, sphere.equals(null));
		assertEquals(false, sphere.equals(new SphereVolume(Point.ORIGIN, 42)));
	}
}
