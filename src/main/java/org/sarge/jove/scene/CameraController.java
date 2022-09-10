package org.sarge.jove.scene;

import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Position;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;

/**
 * The <i>camera controller</i> rotates the scene about the camera position, i.e. a free-look controller.
 * @author Sarge
 */
public class CameraController {
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
	public CameraController(Camera cam, Dimensions dim) {
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
		// TODO - prepare inverse and multiply, move to helper?
		final float yaw = horizontal.interpolate(x / dim.width());
		final float pitch = vertical.interpolate(y / dim.height());
		final Vector vec = Sphere.vector(yaw, pitch);
		update(vec);
	}

	/**
	 * Updates the camera for the given position.
	 * @param pos Position
	 */
	public void update(Position pos) {
		update(pos.x(), pos.y());
	}
	// TODO - remove to bindings

	/**
	 * Updates the camera.
	 * @param vec Unit-sphere vector
	 */
	protected void update(Vector vec) {
		cam.direction(vec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(cam).build();
	}
}
