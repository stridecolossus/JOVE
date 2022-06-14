package org.sarge.jove.scene;

import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;

/**
 * The <i>default camera controller</i> (or free-look mouse) rotates the scene about the camera position.
 * @author Sarge
 */
public class DefaultCameraController {
	protected final Camera cam;
	private final Dimensions dim;
	private final Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
	private final Interpolator vertical = Interpolator.linear(-HALF_PI, HALF_PI);
	// TODO - make interpolator ranges mutable?

	/**
	 * Constructor.
	 * @param cam 	Camera
	 * @param dim 	View dimensions
	 */
	public DefaultCameraController(Camera cam, Dimensions dim) {
		this.cam = notNull(cam);
		this.dim = notNull(dim);
	}

	/**
	 * Updates the camera for the given view coordinates.
	 * @param x
	 * @param y
	 * @see #update(Vector)
	 */
	public void update(float x, float y) {
		final float yaw = horizontal.interpolate(x / dim.width());
		final float pitch = vertical.interpolate(y / dim.height());
		final Vector vec = Sphere.vector(yaw, pitch);
		update(vec);
	}

	/**
	 * Updates the camera.
	 * @param vec Unit-sphere vector
	 */
	protected void update(Vector vec) {
		cam.direction(vec);
	}
}
