package org.sarge.jove.scene;

import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Sphere;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;

/**
 * The <i>default camera controller</i> (or free-look mouse) rotates the scene about the camera position.
 * @author Sarge
 */
public class DefaultCameraController {
	protected final Camera cam;

	private final Dimensions dim;
	private final Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
	private final Interpolator vertical = Interpolator.linear(-HALF_PI, HALF_PI);
	// TODO - make these mutable?

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
	 * @see #update(Point)
	 */
	public void update(float x, float y) {
		// Transform position to angles
		final float yaw = horizontal.interpolate(x / dim.width());
		final float pitch = vertical.interpolate(y / dim.height());

		// Calc point on the unit-sphere
		final Point pt = Sphere.pointRotated(yaw, pitch);
		update(Sphere.swizzle(pt));
	}

	/**
	 * Updates the camera.
	 * @param pos Point of the unit-sphere
	 */
	protected void update(Point pos) {
		cam.direction(new Vector(pos));
	}
}
