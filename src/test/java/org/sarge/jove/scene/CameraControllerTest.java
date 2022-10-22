package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Position;
import org.sarge.jove.geometry.*;

public class CameraControllerTest {
	private Camera cam;
	private CameraController controller;

	@BeforeEach
	void before() {
		cam = new Camera();
		controller = new CameraController(cam, new Dimensions(2, 2));
	}

	@Test
	void update() {
		controller.update(1, 1);
		assertEquals(Axis.Z.vector(), cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
	}

	@Test
	void position() {
		controller.update(new Position(1, 1));
		assertEquals(Axis.Z.vector(), cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
	}
}
