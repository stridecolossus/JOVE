package org.sarge.jove.scene;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.positive;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Sphere;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.Interpolator;
import org.sarge.jove.util.MathsUtil;

/**
 * An <i>orbital camera controller</i>
 * TODO
 */
public class OrbitalCameraController {
	/**
	 * An <i>orbit</i> defines the range and scale of the orbit radius.
	 */
	public static final class Orbit {
		private final float min;
		private final float max;
		private final float scale;

		private float radius;

		/**
		 * Default constructor.
		 */
		public Orbit() {
			this(1, Integer.MAX_VALUE, 1);
		}

		/**
		 * Constructor.
		 * @param min		Minimum radius
		 * @param max		Maximum radius
		 * @param scale		Zoom scalar
		 */
		public Orbit(float min, float max, float scale) {
			if(min >= max) throw new IllegalArgumentException("Invalid zoom range");
			this.min = positive(min);
			this.max = max;
			this.scale = positive(scale);
			this.radius = min;
		}

		/**
		 * Increments this radius and clamps to the specified range.
		 * @param inc Radius increment
		 * @return Actual increment
		 */
		private float zoom(float inc) {
			final float prev = radius;
			radius = MathsUtil.clamp(prev + inc * scale, min, max);
			return radius - prev;
		}
	}

	// http://asliceofrendering.com/camera/2019/11/30/ArcballCamera/
	// https://learnopengl.com/Getting-started/Camera

	// Camera
	private final Camera cam;
	private final Orbit orbit;
	private Point target = Point.ORIGIN;

	// Position
	private final Dimensions dim;
	private Interpolator horizontal = Interpolator.linear(0, MathsUtil.TWO_PI);
	private Interpolator vertical = Interpolator.linear(-MathsUtil.HALF_PI, MathsUtil.HALF_PI);

	/**
	 * Constructor.
	 * @param cam 			Camera
	 * @param dim 			View dimensions
	 * @param orbit			Orbital radius
	 */
	public OrbitalCameraController(Camera cam, Dimensions dim, Orbit orbit) {
		this.cam = notNull(cam);
		this.dim = notNull(dim);
		this.orbit = notNull(orbit);
		init();
	}

	/**
	 * Initialises the camera position.
	 */
	private void init() {
		final var pos = cam.direction().invert().scale(orbit.radius);
		cam.move(target.add(pos));
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
	}

	/**
	 * @return Orbit radius
	 */
	public float radius() {
		return orbit.radius;
	}

	/**
	 * Sets the radius of the orbit.
	 * @param radius New radius
	 * @throws IllegalArgumentException if the radius is out-of-range
	 */
	public void radius(float radius) {
		Check.range(radius, orbit.min, orbit.max);
		orbit.radius = radius;
		init();
	}

	/**
	 * Zooms the radius by the given value.
	 * @param inc Zoom increment
	 */
	public void zoom(float inc) {
		final float actual = orbit.zoom(-inc);
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
		final Point pos = Sphere.point(phi - MathsUtil.HALF_PI, theta, orbit.radius);
		cam.move(target.add(pos));
		cam.look(target);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("target", target)
				.append("radius", orbit.radius)
				.append("camera", cam)
				.build();
	}
}
