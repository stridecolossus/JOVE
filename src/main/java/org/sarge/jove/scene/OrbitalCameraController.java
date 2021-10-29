package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Sphere;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * An <i>orbital camera controller</i> rotates the camera about a <i>target</i> location at a specified <i>radius</i>.
 * <p>
 * Usage:
 * <pre>
 * 	// Init controller
 * 	Camera Camera = ...
 * 	Dimensions viewport = ...
 * 	OrbitalCameraController controller = new OrbitalCameraController(cam, viewport);
 * 	controller.radius(3);
 * 	controller.scale(0.25f);
 *
 * 	...
 *
 * 	// Zoom
 * 	controller.zoom(1);
 *
 * 	// Update on mouse move
 * 	controller.update(x, y);
 * </pre>
 * <p>
 * @author Sarge
 */
public class OrbitalCameraController {
	// http://asliceofrendering.com/camera/2019/11/30/ArcballCamera/
	// https://learnopengl.com/Getting-started/Camera

	// Camera
	private final Camera cam;
	private Point target = Point.ORIGIN;

	// Orbit
	private float min = 1;
	private float max = Integer.MAX_VALUE;
	private float scale = 1;
	private float radius = 1;

	// Position
	private final Dimensions dim;
	private final Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
	private final Interpolator vertical = Interpolator.linear(-MathsUtil.HALF_PI, MathsUtil.HALF_PI);

	/**
	 * Constructor.
	 * @param cam 	Camera
	 * @param dim 	View dimensions
	 */
	public OrbitalCameraController(Camera cam, Dimensions dim) {
		this.cam = notNull(cam);
		this.dim = notNull(dim);
		init();
		cam.look(target);
	}

	/**
	 * Sets the camera position after a change to the orbit radius.
	 */
	private void init() {
		final Vector pos = cam.direction().multiply(radius);
		cam.move(target.add(pos));
	}

	/**
	 * @return Orbit target position
	 */
	public Point target() {
		return target;
	}

	/**
	 * Sets the orbit target.
	 * @throws IllegalArgumentException if the target is the same as the camera position
	 */
	public void target(Point target) {
		// TODO - test
		final float r = MathsUtil.sqrt(cam.position().distance(target));
		this.radius = MathsUtil.clamp(r, min, max);
		this.target = target;
		init();
		cam.look(target);
	}

	/**
	 * @return Orbit radius
	 */
	public float radius() {
		return radius;
	}

	/**
	 * Sets the orbit radius.
	 * @param radius New radius
	 * @throws IllegalArgumentException if the radius is out-of-range
	 */
	public void radius(float radius) {
		this.radius = Check.range(radius, min, max);
		init();
	}

	/**
	 * Sets the range of the orbit radius.
	 * @param min
	 * @param max
	 * @throws IllegalArgumentException if the minimum is zero or greater-than the maximum
	 */
	public void range(float min, float max) {
		Check.positive(min);
		if((min >= max) || (min > radius)) throw new IllegalArgumentException("Invalid zoom range");
		this.min = min;
		this.max = max;
	}

	/**
	 * Sets the zoom scalar.
	 * @param scale Zoom scalar
	 */
	public void scale(float scale) {
		this.scale = Check.positive(scale);
	}

	/**
	 * Zooms by the given increment.
	 * Note that a positive value moves the view towards the target, i.e. decrements the orbital radius.
	 * @param inc Zoom increment
	 */
	public void zoom(float inc) {
		this.radius = MathsUtil.clamp(radius - inc * scale, min, max);
		init();
	}

	/**
	 * Updates the camera for the given view coordinates.
	 * @param x
	 * @param y
	 */
	public void update(float x, float y) {
		// Transform position to angles
		final float yaw = horizontal.interpolate(x / dim.width());
		final float pitch = vertical.interpolate(y / dim.height());

		// Calc point on the unit-sphere
		final Point surface = Sphere.pointRotated(yaw, pitch);
		final Point pos = Sphere.swizzle(surface).scale(radius);

		// Update camera
		cam.move(target.add(pos));
		cam.look(target);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("target", target)
				.append("radius", radius)
				.append("camera", cam)
				.build();
	}
}
