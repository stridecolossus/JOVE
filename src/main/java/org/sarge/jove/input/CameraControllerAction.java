package org.sarge.jove.input;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Location;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.scene.Camera;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Camera controller.
 * @author Sarge
 */
public class CameraControllerAction implements Action {
	private final Camera cam;
	private final CameraController controller;
	private final Dimensions dim;

	private float angle = 90f;
	private float sensitivity = 0.025f;

	/**
	 * Constructor using {@link DefaultCameraController#DEFAULT} controller.
	 * @param cam	Camera
	 * @param rect	Screen dimensions
	 */
	public CameraControllerAction(Camera cam, Dimensions dim) {
		this(cam, dim, CameraController.DEFAULT);
	}

	/**
	 * Constructor.
	 * @param cam			Camera
	 * @param dim			Screen dimensions
	 * @param controller	Controller logic
	 */
	public CameraControllerAction(Camera cam, Dimensions dim, CameraController controller) {
		Check.notNull(cam);
		Check.notNull(dim);
		Check.notNull(controller);

		this.cam = cam;
		this.dim = dim;
		this.controller = controller;
	}

	/**
	 * @return Maximum camera angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Sets the maximum camera angle.
	 * @param angle Camera angle (degrees)
	 */
	public void setAngle(float angle) {
		Check.oneOrMore(angle);
		this.angle = angle;
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
		final Location loc = event.getLocation();
		if(loc == null) throw new IllegalArgumentException("Invalid event type: " + event);

		// Calculate rotation angles from mouse drag deltas
		// TODO - how to limit?
		final float dx = loc.getX() * (dim.getWidth() / angle) * sensitivity * MathsUtil.DEGREES_TO_RADIANS;
		final float dy = loc.getY() * (dim.getHeight() / angle) * sensitivity * MathsUtil.DEGREES_TO_RADIANS;

		// Update camera
		final Quaternion rotX = new Quaternion(cam.getUpDirection(), -dx);
		final Quaternion rotY = new Quaternion(cam.getRightAxis(), -dy);
		controller.update(rotX.multiply(rotY), cam);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
