package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.positive;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Sphere;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * An <i>orbital camera controller</i>
 * TODO
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
	private float radius = min;

	// Position
	private Dimensions dim;
	private final Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
	private final Interpolator vertical = Interpolator.linear(-MathsUtil.HALF_PI, MathsUtil.HALF_PI);
	// TODO - either make these constants or mutable

	/**
	 * Constructor.
	 * @param cam 	Camera
	 * @param dim 	View dimensions
	 */
	public OrbitalCameraController(Camera cam, Dimensions dim) {
		this.cam = notNull(cam);
		dimensions(dim);
		init();
	}

	/**
	 * Initialises the camera position.
	 */
	private void init() {
		final var pos = cam.direction().invert().multiply(radius);
		cam.move(target.add(pos));
	}

	/**
	 * Sets the view dimensions.
	 * @param dim View dimensions
	 */
	public void dimensions(Dimensions dim) {
		this.dim = notNull(dim);
	}

	/**
	 * @return Orbit target position
	 */
	public Point target() {
		return target;
	}

	/**
	 * Sets the orbit target position.
	 * @throws IllegalArgumentException if the target is the same as the camera position
	 */
	public void target(Point target) {
		this.target = notNull(target);
		cam.look(target);
		// TODO - need to init radius? and/or set to min?
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
	 */
	public void range(float min, float max) {
		if(min >= max) throw new IllegalArgumentException("Invalid zoom range");
		this.min = positive(min);
		this.max = positive(max);
	}

	/**
	 * Sets the zoom scalar.
	 * @param scale Zoom scalar
	 */
	public void scale(float scale) {
		this.scale = positive(scale);
	}

	/**
	 * Zooms the radius by the given value.
	 * @param inc Zoom increment
	 */
	public void zoom(float inc) {
		// Update radius
		final float prev = radius;
		radius = MathsUtil.clamp(radius - inc * scale, min, max);

		// Move eye position
		final float actual = radius - prev;
		cam.move(actual);
	}

	/**
	 * Updates the camera position for the given view coordinates.
	 * @param x
	 * @param y
	 */
	public void update(float x, float y) {
		final float phi = horizontal.interpolate(x / dim.width());
		final float theta = vertical.interpolate(y / dim.height());
		final Point pt = Sphere.point(phi - MathsUtil.HALF_PI, theta, radius);
		cam.move(target.add(pt));
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
