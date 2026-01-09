package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>camera</i> models the viewers position and orientation.
 * <p>
 * The camera points in the <b>opposite</b> direction to the view, i.e. the {@link #direction()} points <b>out</b> of the screen in the {@link Axis#Z} direction.
 * <p>
 * Also note that this camera is susceptible to <i>gimbal locking</i> if the view direction is set to the {@link #up()} axis of the camera.
 * <p>
 * @author Sarge
 */
public class Camera {
	// Camera location and view
	private Point position = Point.ORIGIN;
	private Normal direction = Axis.Z;
	private Normal up = Axis.Y;
	private float fov = MathsUtility.toRadians(60);

	// Camera axes
	private Normal right;
	private Normal y;
	private Matrix matrix;

	/**
	 * Constructor.
	 */
	public Camera() {
		update();
		assert right.equals(Axis.X);
		assert y.equals(Axis.Y);
	}

	/**
	 * @return Camera position
	 */
	public Point position() {
		return position;
	}

	/**
	 * Moves the camera to a new position.
	 * @param position New position
	 */
	public Camera move(Point position) {
		this.position = requireNonNull(position);
		dirty();
		return this;
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vector Movement vector
	 */
	public Camera move(Vector vector) {
		position = position.add(vector);
		dirty();
		return this;
	}

	/**
	 * Moves the camera by the given distance in the current view direction (towards the viewer).
	 * @param distance Distance to move
	 * @see #direction()
	 */
	public Camera move(float distance) {
		move(direction.multiply(distance));
		return this;
	}

	/**
	 * Moves the camera by the given distance along the current right axis.
	 * @param distance Distance to strafe
	 * @see #right()
	 */
	public Camera strafe(float distance) {
		// TODO - this is the only case dependant on the right axis (except for matrix)? i.e. update() needed here
		update();
		move(right.multiply(distance));
		return this;
	}

	/**
	 * @return Camera view direction
	 */
	public Normal direction() {
		return direction;
	}

	/**
	 * Sets the camera view direction.
	 * @param direction View direction
	 */
	public Camera direction(Normal direction) {
		this.direction = requireNonNull(direction);
		dirty();
		return this;
	}

	/**
	 * Sets the up axis of this camera.
	 * @param up Camera up axis
	 */
	public Camera up(Normal up) {
		this.up = requireNonNull(up);
		dirty();
		return this;
	}

	/**
	 * @return Camera up axis
	 */
	public Normal up() {
		return up;
	}

	/**
	 * @return Camera right axis
	 */
	public Vector right() {
		update();
		return right;
	}

	/**
	 * Updates the camera axes.
	 */
	private void update() {
		right = new Normal(up.cross(direction));
		y = new Normal(direction.cross(right));

		// TODO...
		if(Float.isNaN(right.x) || Float.isNaN(right.y) || Float.isNaN(right.z)) {
			System.out.println("RIGHT "+right);
		}
		if(Float.isNaN(y.x) || Float.isNaN(y.y) || Float.isNaN(y.z)) {
			System.out.println("Y "+y);
		}
		// TODO
	}

	/**
	 * Points the camera at the given target.
	 * @param target Target position
	 * @throws IllegalArgumentException if {@link #target} is the same as the current position of the camera
	 * @throws IllegalStateException if the resultant direction would result in gimbal lock
	 */
	public Camera look(Point target) {
		if(position.equals(target)) {
			throw new IllegalArgumentException("Cannot point camera at its current position");
		}
		this.direction = Vector.between(target, position).normalize();
		dirty();
		return this;
	}

	/**
	 * Marks the camera matrix as dirty.
	 */
	private void dirty() {
		matrix = null;
	}

	/**
	 * Builds the view matrix for this camera.
	 * <p>
	 * The view matrix is the <b>inverse</b> of the cameras world transformation.
	 * i.e. This matrix effectively translates and rotates the world to <i>camera</i> (or view) space.
	 * <p>
	 * For an <i>orthonormal</i> matrix (one that comprises purely rotational and translation components)
	 * this view matrix is the <i>transpose</i> of the rotation combined with the position translation.
	 * <p>
	 * The resultant matrix is:
	 * <pre>
	 * R T
	 * 0 1
	 * </pre>
	 * Where {@code T} is the translation component and {@code R} is the 3x3 rotation with rows comprising the camera axes:
	 * <ul>
	 * <li>right axis</li>
	 * <li>actual <i>up</i> axis</li>
	 * <li>direction (towards the viewer)</li>
	 * </ul>
	 * <p>
	 * @return Camera view matrix
	 */
	public Matrix matrix() {
		if(matrix == null) {
			update();
			build();
		}
		return matrix;
	}

	/**
	 * @return Vertical field-of-view of this camera (radians)
	 */
	public float fov() {
		return fov;
	}

	/**
	 * Sets the vertical field-of-view of this camera.
	 * @param fov Field-of-view (radians)
	 */
	public void fov(float fov) {
		this.fov = fov;
	}

	/**
	 * Builds the camera view matrix.
	 */
	private void build() {
		final Vector translation = new Vector(
				right.dot(position),
				y.dot(position),
				direction.dot(position)
		);

		matrix = new Matrix.Builder()
				.row(0, right)
				.row(1, y)
				.row(2, direction)
				.column(3, translation.invert())
				.set(3, 3, 1)
				.build();
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, direction, up);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Camera that) &&
				this.position.equals(that.position) &&
				this.direction.equals(that.direction) &&
				this.up.equals(that.up);
	}

	@Override
	public String toString() {
		return String.format("Camera[position=%s direction=%s up=%s fov=%f]", position, direction, up, fov);
	}
}
