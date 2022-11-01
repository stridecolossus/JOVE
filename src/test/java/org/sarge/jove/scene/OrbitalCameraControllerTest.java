package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.AxisControl;
import org.sarge.jove.geometry.*;

public class OrbitalCameraControllerTest {
	private static final Vector Z = Axis.Z;

	private Camera cam;
	private OrbitalCameraController controller;

	@BeforeEach
	void before() {
		cam = new Camera();
		controller = new OrbitalCameraController(cam, new Dimensions(2, 2));
	}

	@Test
	void constructor() {
		assertEquals(1, controller.radius());
		assertEquals(Point.ORIGIN, controller.target());
		assertEquals(new Point(0, 0, 1), cam.position());
		assertEquals(Z, cam.direction());
	}

	@Test
	void radius() {
		controller.radius(2);
		assertEquals(2, controller.radius());
		assertEquals(new Point(0, 0, 2), cam.position());
		assertEquals(Z, cam.direction());
	}

	@Test
	void range() {
		controller.range(1, 2);
		assertThrows(IllegalArgumentException.class, () -> controller.radius(3));
	}

	@Test
	void rangeInvalid() {
		assertThrows(IllegalArgumentException.class, () -> controller.range(2, 1));
	}

	@Test
	void target() {
		final Point target = new Point(0, 0, -42);
		controller.target(target);
		assertEquals(target, controller.target());
		assertEquals(Z, cam.direction());
	}

	@Test
	void update() {
		controller.update(1, 1);
		assertEquals(1, controller.radius());
		assertEquals(new Point(0, 0, 1), cam.position());
		assertEquals(Z, cam.direction());
	}

	@Nested
	class ZoomTests {
		@BeforeEach
		void before() {
			controller.range(1, 3);
		}

		@Test
		void zoom() {
			controller.zoom(-1);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), cam.position());
			assertEquals(Z, cam.direction());
		}

		@Test
		void scale() {
			controller.scale(2);
			controller.zoom(-1);
			assertEquals(3, controller.radius());
		}

		@Test
		void zoomClampMinimum() {
			controller.zoom(1);
			assertEquals(1, controller.radius());
			assertEquals(new Point(0, 0, 1), cam.position());
		}

		@Test
		void zoomClampMaximum() {
			controller.zoom(-999);
			assertEquals(3, controller.radius());
			assertEquals(new Point(0, 0, 3), cam.position());
		}

		@Test
		void axis() {
			final AxisControl axis = mock(AxisControl.class);
			when(axis.value()).thenReturn(-1f);
			controller.zoom(axis);
			assertEquals(2, controller.radius());
			assertEquals(new Point(0, 0, 2), cam.position());
			assertEquals(Z, cam.direction());
		}
	}
}
