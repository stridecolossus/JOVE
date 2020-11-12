package org.sarge.jove.scene;

import static org.sarge.jove.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>camera</i> represents a viewers position and orientation.
 * @author Sarge
 */
public class Camera {
	// Camera state
	private Point pos = Point.ORIGIN;
	private Vector dir = Vector.Z_AXIS.invert();

	// Axes
	private Vector up = Vector.Y_AXIS;
	private transient Vector right;

	// Matrix
	private transient Matrix matrix;
	private transient boolean dirty;

	// https://learnopengl.com/Getting-started/Camera

	/**
	 * Constructor.
	 */
	public Camera() {
		update();
	}

	/**
	 * @return Camera position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Moves the camera to a new position.
	 * @param pos New position
	 */
	public void move(Point pos) {
		this.pos = notNull(pos);
		dirty();
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vec Movement vector
	 */
	public void move(Vector vec) {
		pos = pos.add(vec);
		dirty();
	}

	/**
	 * Moves the camera by the given distance in the current view direction.
	 * @param dist Distance to move
	 * @see #direction()
	 */
	public void move(float dist) {
		move(dir.scale(-dist));
	}

	/**
	 * Moves the camera by the given distance in the current right axis.
	 * @param dist Distance to strafe
	 * @see #right()
	 */
	public void strafe(float dist) {
		move(right.scale(dist));
	}

	/**
	 * @return Camera view direction
	 */
	public Vector direction() {
		return dir;
	}

	/**
	 * Sets the camera view direction.
	 * @param dir View direction (assumes normalized)
	 */
	public void direction(Vector dir) {
		this.dir = notNull(dir);
		dirty();
	}

	/**
	 * Points the camera at the given location.
	 * @param pt Camera point-of-interest
	 * @throws IllegalArgumentException if the location is the same as the current position of the camera
	 */
	public void look(Point pt) {
		if(pos.equals(pt)) throw new IllegalArgumentException("Cannot point camera at its current position");
		dir = Vector.of(pt, pos).normalize();
		dirty();
	}

	/**
	 * Sets the up axis of this camera (default is {@link Vector#Y_AXIS}).
	 * @param up Camera up axis (assumes normalized)
	 */
	public void up(Vector up) {
		this.up = notNull(up);
		dirty();
	}

	/**
	 * @return Camera up axis
	 */
	public Vector up() {
		return up;
	}

	/**
	 * @return Camera right axis
	 */
	public Vector right() {
		return right;
	}

	/**
	 * Sets the camera orientation to the given yaw and pitch angles (radians).
	 * @param yaw		Yaw
	 * @param pitch		Pitch
	 */
	public void orientation(float yaw, float pitch) {
		final float cos = MathsUtil.cos(pitch);
		final float x = MathsUtil.cos(yaw) * cos;
		final float y = MathsUtil.sin(pitch);
		final float z = MathsUtil.sin(-yaw) * cos;
		final Vector dir = new Vector(x, y, z).normalize();
		direction(dir);
	}

	/**
	 * Creates an orbital controller for this camera.
	 * @param rect View rectangle
	 * @return Orbital controller
	 */
	public OrbitalController orbital(Dimensions dim) {
		final var controller = new OrbitalController();
		controller.dimensions(dim);
		return controller;
	}

	/**
	 * Rotates the camera.
	 * @param rot Rotation
	 * @see Quaternion#rotate(Vector)
	 */
	public void rotate(Rotation rot) {
		// TODO
		dir = Quaternion.of(rot).rotate(dir);
		dirty();
	}

	/**
	 * Marks the camera matrix as dirty.
	 */
	private void dirty() {
		dirty = true;
	}

	/**
	 * @return Camera view matrix
	 */
	public Matrix matrix() {
		if(dirty) {
			update();
			dirty = false;
		}
		return matrix;
	}

	/**
	 * Updates the camera axes and matrix.
	 */
	private void update() {
		// Determine right axis
		right = dir.cross(up).normalize();

		// Determine up axis
		final Vector y = right.cross(dir).normalize();

		// Calculate translation component
		final Matrix trans = Matrix.translation(new Vector(pos).invert());

		// Build rotation matrix
		final Matrix rot = new Matrix.Builder()
			.identity()
			.row(0, right)
			.row(1, y)
			.row(2, dir)
			.build();

		// Create camera matrix
		matrix = rot.multiply(trans);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pos", pos)
				.append("dir", dir)
				.append("up", up)
				.build();
	}

	/**
	 * An <i>orbital camera controller</i>
	 * TODO
	 */
	public class OrbitalController {
		private Dimensions dim;
		private Point target = Point.ORIGIN;
		private float radius = 1;

		private OrbitalController() {
		}

		/**
		 * Sets the view dimensions for this controller.
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
			look(target);
		}

		/**
		 * @return Current orbit radius
		 */
		public float radius() {
			return radius;
		}

		/**
		 * Sets the orbit radius.
		 * @param radius Radius
		 * @throws IllegalArgumentException if the radius is not greater-than-zero
		 */
		public void radius(float radius) {
			if(radius <= 0) throw new IllegalArgumentException("Radius must be greater-than zero");
			this.radius = radius;
			dirty();
		}

		/**
		 * Increments the orbit radius by the given value.
		 * @param zoom Radius increment
		 * @throws IllegalArgumentException if the radius is not greater-than-zero
		 */
		public void zoom(float zoom) {
			zoom = -zoom / 10f;
System.out.println("zoom="+zoom);
if(radius + zoom < 0.75f) return; // TODO
			//if(radius + zoom <= 0) throw new IllegalArgumentException("Radius must be greater-than zero");
			move(-zoom);
			this.radius += zoom;
System.out.println("radius="+radius);
			//dirty();
		}

		// TODO - min/max radius
		// TODO - zoom scale (is +1 or -1 from GLFW per tick)

		/**
		 * Updates the camera position for the given view coordinates.
		 * @param x
		 * @param y
		 * TODO
		 */
		public void update(float x, float y) {
			// TODO - configurable ranges? scale factors?
			final float dx = x / dim.width() * MathsUtil.TWO_PI;
			final float dy = y / dim.height() * MathsUtil.PI;
			final Point pos = new Point(MathsUtil.sin(dx) * radius, MathsUtil.cos(dy), MathsUtil.cos(dx) * radius);
			move(pos);
			look(target);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("target", target)
					.append("radius", radius)
					.append("camera", Camera.this)
					.build();
		}
	}
}
