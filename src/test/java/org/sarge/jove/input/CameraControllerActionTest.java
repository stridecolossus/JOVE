package org.sarge.jove.input;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Location;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.util.MathsUtil;

public class CameraControllerActionTest {
	private CameraControllerAction action;
	private CameraController controller;
	private Camera cam;

	@Before
	public void before() {
		cam = mock(Camera.class);
		controller = CameraController.DEFAULT;
		action = new CameraControllerAction(cam, new Dimensions(640, 480), controller);
	}

	@Test
	public void execute() {
		// Create drag action
		final Location loc = new Location(10, 0);
		final EventKey key = EventKey.POOL.get();
		key.init(EventType.DRAG, "Button1");
		final InputEvent event = InputEvent.POOL.get();
		event.init(mock(Device.class), key);
		event.setLocation(loc);

		// Mock camera
		when(cam.getUpDirection()).thenReturn(Vector.Y_AXIS);
		when(cam.getRightAxis()).thenReturn(Vector.X_AXIS);

		// Invoke action
		action.setSensitivity(1);
		action.execute(event);

		// Verify camera is rotated
		final Rotation rot = new Rotation(Vector.Y_AXIS, MathsUtil.toRadians(-10f * 640f / 90f));
		verify(cam).rotate(new Quaternion(rot));
	}
}
