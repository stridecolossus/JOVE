package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.OrbitalCameraController.Orbit;

public class OrbitalCameraControllerTest {
	private Camera cam;
	private OrbitalCameraController controller;

	@BeforeEach
	void before() {
		cam = new Camera();
		controller = new OrbitalCameraController(cam, new Dimensions(2, 2), new Orbit(1, 2, 1));
	}

	@Test
	void invalidOrbitRange() {
		assertThrows(IllegalArgumentException.class, () -> new Orbit(2, 1, 1));
	}

	@Test
	void constructor() {
		assertEquals(1, controller.radius());
		assertEquals(Point.ORIGIN, controller.target());
		assertEquals(new Point(0, 0, 1), cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
	}

	@Test
	void target() {
		final Point target = new Point(0, 0, -42);
		controller.target(target);
		assertEquals(target, controller.target());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
	}

	@Test
	void update() {
		controller.update(1, 1);
		assertEquals(1, controller.radius());
		assertEquals(new Point(0, 0, 1), cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
	}

	@Test
	void radius() {
		controller.radius(2);
		assertEquals(2, controller.radius());
		assertEquals(new Point(0, 0, 2), cam.position());
		assertEquals(Vector.Z_AXIS.invert(), cam.direction());
	}

	@Nested
	class ZoomTests {
		@Test
		void zoom() {
			controller.zoom(-1);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), cam.position());
			assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		}

		@Test
		void zoomClampMinimum() {
			controller.zoom(1);
			assertEquals(1, controller.radius());
			assertEquals(new Point(0, 0, 1), cam.position());
			assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		}

		@Test
		void zoomClampMaximum() {
			controller.zoom(-42);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), cam.position());
			assertEquals(Vector.Z_AXIS.invert(), cam.direction());
		}
	}
}
