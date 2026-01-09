package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Z;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;

class OrbitalCameraControllerTest {
	private Camera camera;
	private OrbitalCameraController controller;

	@BeforeEach
	void before() {
		camera = new Camera();
		controller = new OrbitalCameraController(camera, new Dimensions(640, 480));
	}

	@Test
	void constructor() {
		assertEquals(1, controller.radius());
		assertEquals(Point.ORIGIN, controller.target());
		assertEquals(new Point(0, 0, 1), camera.position());
		assertEquals(Z, camera.direction());
	}

	@Test
	void target() {
		final Point target = new Point(0, 0, -3);
		controller.target(target);
		assertEquals(target, controller.target());
		assertEquals(Z, camera.direction());
	}

	@Nested
	class UpdateTest {
		@Test
		void ahead() {
			controller.update(320, 240);
			assertEquals(1, controller.radius());
			assertEquals(new Point(0, 0, 1), camera.position());
			assertEquals(Z, camera.direction());
		}

		@Test
		void left() {
			controller.update(480, 240);
			assertEquals(1, controller.radius());
			assertEquals(new Point(-1, 0, 0), camera.position());
			assertEquals(Axis.X.invert(), camera.direction());
		}

		@Test
		void behind() {
			controller.update(640, 240);
			assertEquals(1, controller.radius());
			assertEquals(new Point(0, 0, -1), camera.position());
			assertEquals(Z.invert(), camera.direction());
		}
	}

	@Nested
	class RadiusTest {
		@Test
		void radius() {
			controller.radius(2);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), camera.position());
			assertEquals(Z, camera.direction());
		}

		@Test
		void range() {
			controller.range(1, 2);
			assertThrows(IllegalArgumentException.class, () -> controller.radius(3));
		}

		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> controller.range(2, 1));
		}
	}

	@Nested
	class ZoomTest {
		@BeforeEach
		void before() {
			controller.range(1, 3);
		}

		@Test
		void zoom() {
			controller.zoom(-1);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), camera.position());
			assertEquals(Z, camera.direction());
		}

		@Test
		void scale() {
			controller.scale(2);
			controller.zoom(-1);
			assertEquals(3, controller.radius());
		}

		@Test
		void minimum() {
			controller.zoom(1);
			assertEquals(1, controller.radius());
			assertEquals(new Point(0, 0, 1), camera.position());
		}

		@Test
		void maximum() {
			controller.zoom(-999);
			assertEquals(3, controller.radius());
			assertEquals(new Point(0, 0, 3), camera.position());
		}

		@Test
		void handler() {
			controller.zoom().accept(new AxisEvent(-1));
			assertEquals(2, controller.radius());
		}
	}
}
