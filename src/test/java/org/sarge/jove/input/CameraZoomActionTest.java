package org.sarge.jove.input;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.Camera;

public class CameraZoomActionTest {
	private CameraZoomAction action;
	private Camera cam;

	@Before
	public void before() {
		cam = mock(Camera.class);
		action = new CameraZoomAction(cam);
	}

	@Test
	public void execute() {
		// Mock camera
		when(cam.getPosition()).thenReturn(new Point(0, 0, -5));
		when(cam.getDirection()).thenReturn(Vector.Z_AXIS.invert());

		// Create zoom action
		final Device dev = mock(Device.class);
		final InputEvent event = new InputEvent(dev, new EventKey(EventType.ZOOM));
		event.setZoom(2);
		
		// Run action and check position updated
		action.setSensitivity(1);
		action.execute(event);
		verify(cam).setPosition(new Point(0, 0, -3f));
	}
}
