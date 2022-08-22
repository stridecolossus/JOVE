package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

public class CollisionSurfaceTest {
	private CollisionSurface surface;

	@DisplayName("A collision surface defined by a plane...")
	@Nested
	class PlaneTests {
		@BeforeEach
		void before() {
			surface = CollisionSurface.of(Plane.of(Vector.Y, Point.ORIGIN));
			assertNotNull(surface);
		}

		@DisplayName("is intersected by a particle behind the plane")
		@Test
		void inside() {
			assertEquals(true, surface.intersects(new Point(0, -1, 0)));
		}

		@DisplayName("is intersected by a particle on the plane")
		@Test
		void intersects() {
			assertEquals(true, surface.intersects(Point.ORIGIN));
		}

		@DisplayName("is not intersected by a particle in front of the plane")
		@Test
		void outside() {
			assertEquals(false, surface.intersects(new Point(0, 1, 0)));
		}
	}

	@DisplayName("A collision surface defined by a bounding volume...")
	@Nested
	class VolumeTests {
		@BeforeEach
		void before() {
			surface = CollisionSurface.of(new SphereVolume(Point.ORIGIN, 1));
			assertNotNull(surface);
		}

		@DisplayName("is intersected by a particle inside the volume")
		@Test
		void inside() {
			assertEquals(true, surface.intersects(Point.ORIGIN));
		}

		@DisplayName("is intersected by a particle touching the volume")
		@Test
		void touching() {
			assertEquals(true, surface.intersects(new Point(1, 0, 0)));
		}

		@DisplayName("is not intersected by a particle outside the volume")
		@Test
		void outside() {
			assertEquals(false, surface.intersects(new Point(2, 0, 0)));
		}
	}
}
