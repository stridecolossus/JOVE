package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.*;
import org.sarge.jove.geometry.*;

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

	@Test
	void position() {
		@SuppressWarnings("unchecked")
		final Event.Source<PositionEvent> src = mock(Event.Source.class);
		controller.update(new PositionEvent(src, 1, 1));
		assertEquals(Vector.Z, cam.direction());
		assertEquals(Point.ORIGIN, cam.position());
	}
}
