package org.sarge.jove.control;

import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.util.function.Consumer;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * An <i>orbital camera controller</i> rotates the camera <b>about</b> a target position at a configured radius.
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
	 * @param camera 		Camera
	 * @param dimensions 	View dimensions
	 */
	public OrbitalCameraController(Camera camera, Dimensions dimensions) {
		super(camera, dimensions);
		init();
		camera.look(target);
	}

	/**
	 * Sets the camera position after a change to the orbital radius.
	 */
	private void init() {
		final Camera camera = super.camera();
		final Vector pos = camera.direction().multiply(radius);
		camera.move(target.add(pos));
	}

	@Override
	protected void update(Normal direction) {
		final Point position = target.add(direction.multiply(radius));
		final Camera camera = super.camera();
		camera.move(position);
		camera.direction(direction);
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
	 * @throws IllegalArgumentException if {@link #target} is the same as the current camera position
	 */
	public void target(Point target) {
		final Camera camera = super.camera();
		final float radius = MathsUtility.sqrt(camera.position().distance(target));
		this.radius = MathsUtility.clamp(radius, min, max);
		this.target = target;
		init();
		camera.look(target);
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
	 * @see #range(float, float)
	 */
	public void radius(float radius) {
		if((radius < min) || (radius > max)) {
			throw new IllegalArgumentException("Invalid radius: radius=%d range=%d/%d".formatted(radius, min, max));
		}
		this.radius = radius;
		init();
	}

	/**
	 * Sets the range of the orbit radius.
	 * @throws IllegalArgumentException if {@link #min} is negative or greater than {@link #max}
	 */
	public void range(float min, float max) {
		requireZeroOrMore(min);
		if((min >= max) || (min > radius)) {
			throw new IllegalArgumentException("Invalid zoom range");
		}
		this.min = min;
		this.max = max;
	}

	/**
	 * Sets the zoom scalar.
	 * @param scale Zoom scalar
	 * @see #zoom(float)
	 */
	public void scale(float scale) {
		if(scale <= 0) {
			throw new IllegalArgumentException("Zoom scale must be positive");
		}
		this.scale = scale;
	}

	/**
	 * Zooms by the given increment.
	 * Note that a positive value moves the view towards the target, i.e. decrements the orbital radius.
	 * @param zoom Zoom increment
	 * @see #scale(float)
	 */
	public void zoom(float zoom) {
		this.radius = MathsUtility.clamp(radius - zoom * scale, min, max);
		init();
	}

	/**
	 * Helper.
	 * Creates an event handler bind-point for the radius zoom.
	 * @return Zoom event handler
	 * @see #zoom(float)
	 */
	public Consumer<AxisEvent> zoom() {
		return event -> zoom(event.value());
	}
}
