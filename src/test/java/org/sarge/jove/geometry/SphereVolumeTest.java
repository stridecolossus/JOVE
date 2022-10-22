package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.geometry.Axis.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtil;

class SphereVolumeTest {
	private static final float RADIUS = 3;
	private static final float OUTSIDE = 4;

	private SphereVolume sphere;

	@BeforeEach
	void before() {
		sphere = new SphereVolume(new Sphere(Point.ORIGIN, RADIUS));
	}

	@Test
	void constructor() {
		assertEquals(new Sphere(Point.ORIGIN, RADIUS), sphere.sphere());
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

	@DisplayName("Intersection tests delegate to a sphere intersection")
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
			assertEquals(true, sphere.intersects(new Plane(X.vector(), 0)));
			assertEquals(true, sphere.intersects(new Plane(Y.vector(), 0)));
			assertEquals(true, sphere.intersects(new Plane(Z.vector(), 0)));
		}

		@Test
		void inside() {
			assertEquals(true, sphere.intersects(new Plane(X.vector(), 1)));
			assertEquals(true, sphere.intersects(new Plane(X.vector(), -1)));
		}

		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new Plane(X.vector(), RADIUS)));
			assertEquals(true, sphere.intersects(new Plane(X.vector(), -RADIUS)));
		}

		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new Plane(X.vector(), OUTSIDE)));
			assertEquals(false, sphere.intersects(new Plane(X.vector(), -OUTSIDE)));
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
			assertEquals(true, sphere.intersects(new SphereVolume(new Sphere(Point.ORIGIN, 1))));
		}

		@DisplayName("Overlapping spheres should intersect")
		@Test
		void intersecting() {
			assertEquals(true, sphere.intersects(new SphereVolume(new Sphere(outside, 2))));
		}

		@DisplayName("Touching spheres intersect")
		@Test
		void touching() {
			assertEquals(true, sphere.intersects(new SphereVolume(new Sphere(outside, 1))));
		}

		@DisplayName("Non-intersecting spheres")
		@Test
		void outside() {
			assertEquals(false, sphere.intersects(new SphereVolume(new Sphere(outside, MathsUtil.HALF))));
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
		@DisplayName("Sphere behind, does not intersect")
		@Test
		void behindNotIntersecting() {
			assertEquals(Intersection.NONE, sphere.intersection(new DefaultRay(new Point(OUTSIDE, 0, 0), X.vector())));
		}

		@DisplayName("Sphere behind, ray originates inside the sphere")
		@Test
		void behindInside() {
			final Ray ray = new DefaultRay(new Point(1, 0, 0), X.vector());
			final Intersection intersection = sphere.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{2}, intersection.distances());
			assertEquals(X.vector(), intersection.normal(ray.point(2)));
		}

		@DisplayName("Sphere behind, ray originates on the sphere surface")
		@Test
		void behindTouching() {
			final Ray ray = new DefaultRay(new Point(RADIUS, 0, 0), X.vector());
			final Intersection intersection = sphere.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{0}, intersection.distances());
			assertEquals(X.vector(), intersection.normal(ray.point(0)));
		}

		@DisplayName("Sphere ahead, does not intersect")
		@Test
		void outside() {
			assertEquals(Intersection.NONE, sphere.intersection(new DefaultRay(new Point(-OUTSIDE, +OUTSIDE, 0), X.vector())));
		}

		@DisplayName("Sphere ahead, ray originates inside the sphere")
		@Test
		void inside() {
			final Ray ray = new DefaultRay(new Point(-1, 0, 0), X.vector());
			final Intersection intersection = sphere.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{4}, intersection.distances());
			assertEquals(X.vector(), intersection.normal(ray.point(4)));
		}

		@DisplayName("Sphere ahead, intersects twice")
		@Test
		void intersects() {
			final Ray ray = new DefaultRay(new Point(-OUTSIDE, 0, 0), X.vector());
			final Intersection intersection = sphere.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{1, 7}, intersection.distances());
			assertEquals(X.vector().invert(), intersection.normal(ray.point(1)));
			assertEquals(X.vector(), intersection.normal(ray.point(7)));
		}

		@DisplayName("Sphere ahead, ray originates on the sphere surface")
		@Test
		void touching() {
			final Ray ray = new DefaultRay(new Point(-RADIUS, 0, 0), X.vector());
			final Intersection intersection = sphere.intersection(ray);
			assertEquals(false, intersection.isEmpty());
			assertArrayEquals(new float[]{0, 6}, intersection.distances());
			assertEquals(X.vector().invert(), intersection.normal(ray.point(0)));
			assertEquals(X.vector(), intersection.normal(ray.point(6)));
		}
	}

	@Test
	void equals() {
		assertEquals(sphere, sphere);
		assertEquals(sphere, new SphereVolume(new Sphere(Point.ORIGIN, RADIUS)));
		assertNotEquals(sphere, null);
		assertNotEquals(sphere, new SphereVolume(new Sphere(Point.ORIGIN, 999)));
	}
}
