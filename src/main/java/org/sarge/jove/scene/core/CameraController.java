package org.sarge.jove.scene.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.MathsUtility.*;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Position;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Sphere.NormalFactory;

/**
 * The <i>camera controller</i> rotates the scene about the cameras position, i.e. a free-look controller.
 * @author Sarge
 */
public class CameraController {
	protected final Camera cam;
	private final Dimensions dim;
	private final NormalFactory factory;
//	private final Interpolator horizontal = Interpolator.linear(0, TWO_PI);
//	private final Interpolator vertical = Interpolator.linear(-HALF_PI, HALF_PI);
	// TODO - make interpolator ranges mutable?

	/**
	 * Constructor.
	 * @param camera 	Camera
	 * @param dim 		View dimensions
	 * @param factory	Sphere normals factory
	 */
	public CameraController(Camera camera, Dimensions dim, NormalFactory factory) {
		this.cam = requireNonNull(camera);
		this.dim = requireNonNull(dim);
		this.factory = factory.rotate();
	}

	public CameraController(Camera camera, Dimensions dim) {
		this(camera, dim, new NormalFactory());
	}

	/**
	 * Updates the camera for the given view coordinates.
	 * @see #update(Normal)
	 */
	public void update(float x, float y) {
// TODO - prepare inverse and multiply, move to helper?
//		final float yaw = horizontal.apply(x / dim.width());
//		final float pitch = vertical.apply(y / dim.height());
		final float yaw = lerp(x, 0, TWO_PI);
		final float pitch = lerp(y, -HALF_PI, +HALF_PI);
		final Vector vec = factory.vector(yaw, pitch);
		update(new Normal(vec));
	}

	// TODO
	static float lerp(float t, float start, float end) {
		// TODO - Math.fma(end - start, t, start);
		return start + (end - start) * t;
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
}
