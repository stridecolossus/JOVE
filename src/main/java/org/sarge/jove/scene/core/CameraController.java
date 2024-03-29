package org.sarge.jove.scene.core;

import static org.sarge.jove.util.Trigonometric.*;
import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Position;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Interpolator;

/**
 * The <i>camera controller</i> rotates the scene about the cameras position, i.e. a free-look controller.
 * @author Sarge
 */
public class CameraController {
	protected final Camera cam;
	private final Dimensions dim;
	private final Interpolator horizontal = Interpolator.linear(0, TWO_PI);
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
	 * @see #update(Normal)
	 */
	public void update(float x, float y) {
		// TODO - prepare inverse and multiply, move to helper?
		final float yaw = horizontal.apply(x / dim.width());
		final float pitch = vertical.apply(y / dim.height());
		final Vector vec = Sphere.vector(yaw, pitch);
		update(new Normal(vec));
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
	 * @param dir View direction
	 */
	protected void update(Normal dir) {
		cam.direction(dir);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(cam).build();
	}
}
