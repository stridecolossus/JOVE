package org.sarge.jove.input;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.Camera;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Camera zoom controller.
 * @author Sarge
 */
public class CameraZoomAction implements Action {
	private final Camera cam;

	private float sensitivity = 0.25f;

	/**
	 * Constructor.
	 * @param cam Camera
	 */
	public CameraZoomAction(Camera cam) {
		Check.notNull(cam);
		this.cam = cam;
	}

	/**
	 * @return Camera sensitivity
	 */
	public float getSensitivity() {
		return sensitivity;
	}

	/**
	 * Sets the rotation sensitivity.
	 * @param sensitivity
	 */
	public void setSensitivity(float sensitivity) {
		this.sensitivity = sensitivity;
	}

	@Override
	public void execute(InputEvent event) {
		// Verify event
		if(event.getEventKey().getType() != EventType.ZOOM) throw new IllegalArgumentException("Expected zoom event");

		// Calc zoom vector
		final Vector dir = cam.getDirection().multiply(-event.getZoom() * sensitivity);

		// Add to camera
		cam.setPosition(cam.getPosition().add(dir));
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
