package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Axis;

class CameraControllerTest {
	private Camera camera;
	private CameraController controller;

	@BeforeEach
	void before() {
		camera = new Camera();
		controller = new CameraController(camera, new Dimensions(640, 480));
	}

	@Test
	void ahead() {
		controller.update(320, 240);
		assertEquals(Axis.Z, camera.direction());
	}

	@Test
	void behind() {
		controller.update(0, 240);
		assertEquals(Axis.Z.invert(), camera.direction());
	}

	@Test
	void handler() {
		controller.position().accept(new ScreenCoordinate(320, 240));
		assertEquals(Axis.Z, camera.direction());
	}
}
