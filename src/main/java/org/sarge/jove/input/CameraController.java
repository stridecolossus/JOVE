package org.sarge.jove.input;

import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.scene.Camera;

/**
 * Camera orientation controller.
 */
@FunctionalInterface
public interface CameraController {
	/**
	 * First-person controller.
	 */
	CameraController DEFAULT = (rot, cam) -> cam.rotate(rot);

	/**
	 * Orbit controller.
	 */
	CameraController ORBIT = new CameraController() {
		@Override
		public void update(Quaternion rot, Camera cam) {
			cam.orbit(rot);
		}
	};

	/**
	 * Updates the camera orientation.
	 * @param rot Rotation
	 * @param cam Camera
	 */
	void update(Quaternion rot, Camera cam);
}
