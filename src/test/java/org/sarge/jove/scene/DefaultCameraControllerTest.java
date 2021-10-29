package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class DefaultCameraControllerTest {
	private Camera cam;
	private DefaultCameraController controller;

	@BeforeEach
	void before() {
		cam = new Camera();
		controller = new DefaultCameraController(cam, new Dimensions(2, 2));
	}

	@Test
	void update() {
		controller.update(1, 1);
		assertEquals(Vector.Z, cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
	}
}
