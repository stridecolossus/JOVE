package org.sarge.jove.scene;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.AxisControl;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * An <i>orbital camera controller</i> rotates the camera about a <i>target</i> position at a specified <i>radius</i>.
 * <p>
 * Usage:
 * <pre>
 * // Init controller
 * Camera Camera = ...
 * Dimensions viewport = ...
 * var controller = new OrbitalCameraController(cam, viewport);
 * controller.radius(3);
 * controller.scale(0.25f);
 *
 * // Zoom
 * controller.zoom(1);
 *
 * // Update on mouse move
 * controller.update(x, y);
 * </pre>
 * <p>
 * @author Sarge
 */
public class OrbitalCameraController extends CameraController {
	private Point target = Point.ORIGIN;
	private float min = 1;
	private float max = Integer.MAX_VALUE;
	private float scale = 1;
	private float radius = 1;

	/**
	 * Constructor.
	 * @param cam 	Camera
	 * @param dim 	View dimensions
	 */
	public OrbitalCameraController(Camera cam, Dimensions dim) {
		super(cam, dim);
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
	 * @param target Target position
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
	 * @see #zoom(float)
	 */
	public void scale(float scale) {
		this.scale = Check.positive(scale);
	}

	/**
	 * Zooms by the given increment.
	 * Note that a positive value moves the view towards the target, i.e. decrements the orbital radius.
	 * @param inc Zoom increment
	 * @see #scale(float)
	 */
	public void zoom(float inc) {
		this.radius = MathsUtil.clamp(radius - inc * scale, min, max);
		init();
	}

	/**
	 * Zooms according to the given axis event.
	 * @param axis Axis
	 */
	public void zoom(AxisControl axis) {
		zoom(axis.value());
	}
	// TODO

	@Override
	protected void update(Normal dir) {
		final Point pos = new Point(dir).multiply(radius).add(target);
		cam.move(pos);
		cam.direction(dir);
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
