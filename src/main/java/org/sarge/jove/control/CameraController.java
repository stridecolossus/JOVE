package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.MathsUtility.*;

import java.util.function.Consumer;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.SphereNormalFactory.DefaultSphereNormalFactory;
import org.sarge.jove.util.Interpolator;

/**
 * A <i>camera controller</i> rotates the scene about the cameras position, i.e. a free-look controller.
 * @author Sarge
 */
public class CameraController {
	private final Camera camera;
	private final Interpolator horizontal = Interpolator.linear(0, TWO_PI);
	private final Interpolator vertical = Interpolator.linear(-HALF_PI, HALF_PI);
	private float dx, dy;
	private SphereNormalFactory sphere = new DefaultSphereNormalFactory().rotate();

	// TODO
	// - constraints on interpolation, i.e. sort of FOV, e.g. H = left -> right, V = +/- 45 degrees
	// - either FOV | constrained dimensions, e.g. 640,480 -> 320,240 | fiddle angles
	// - replace dx/dy with interpolator pair
	// - use compound interpolators?
	// - gimbal locking

	/**
	 * Constructor.
	 * @param camera 		Camera
	 * @param dimensions	View dimensions
	 */
	public CameraController(Camera camera, Dimensions dimensions) {
		this.camera = requireNonNull(camera);
		dimensions(dimensions);
	}

	/**
	 * @return Camera
	 */
	protected Camera camera() {
		return camera;
	}

	/**
	 * Sets the normal factory for the unit-sphere.
	 * @param sphere Sphere normal factory
	 */
	public void sphere(SphereNormalFactory sphere) {
		this.sphere = requireNonNull(sphere);
	}

	/**
	 * Sets the view dimensions.
	 * @param dimensions View dimensions
	 */
	public void dimensions(Dimensions dimensions) {
		this.dx = 1f / dimensions.width();
		this.dy = 1f / dimensions.height();
	}

	/**
	 * Helper.
	 * Creates an event handler bind-point for this controller.
	 * @return Event handler
	 * @see #update(float, float)
	 */
	public Consumer<ScreenCoordinate> position() {
		return coordinate -> update(coordinate.x(), coordinate.y());
	}

	/**
	 * Updates the camera for the given view coordinates.
	 * @see #update(Normal)
	 */
	protected final void update(float x, float y) {
		final float yaw = horizontal.interpolate(x * dx);
		final float pitch = vertical.interpolate(y * dy);
		final Normal normal = sphere.normal(yaw, pitch);
		update(normal);
	}

	/**
	 * Updates the camera.
	 * @param direction View direction
	 */
	protected void update(Normal direction) {
		camera.direction(direction);
	}
}
