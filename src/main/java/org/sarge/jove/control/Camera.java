package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.sarge.jove.geometry.*;

/**
 * A <i>camera</i> models the viewers position and orientation.
 * <p>
 * Note that the camera points in the opposite direction to the view, i.e. the {@link #direction()} points <b>out</b> of the screen in the {@link Axis#Z} direction.
 * <p>
 * @author Sarge
 */
public class Camera {
	// Camera state
	private Point position = Point.ORIGIN;
	private Normal direction = Axis.Z;
	private Normal up = Axis.Y;

	// Transient view transform
	private Normal right = Axis.X;
	private Matrix matrix;
	private boolean dirty = true;

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
	public void move(Point position) {
		this.position = requireNonNull(position);
		dirty();
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vector Movement vector
	 */
	public void move(Vector vector) {
		position = position.add(vector);
		dirty();
	}

	/**
	 * Moves the camera by the given distance in the current view direction.
	 * @param distance Distance to move
	 * @see #direction()
	 */
	public void move(float distance) {
		move(direction.multiply(distance));
	}

	/**
	 * Moves the camera by the given distance along the current right axis.
	 * @param distance Distance to strafe
	 * @see #right()
	 */
	public void strafe(float distance) {
		// TODO - assumes right is valid at this point? what if direction or up have been changed?
		move(right.multiply(distance));
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
	 * @throws IllegalStateException if the direction would result in gimbal lock
	 */
	public void direction(Normal direction) {
		validate(direction, up);
		this.direction = requireNonNull(direction);
		dirty();
	}

	/**
	 * Points the camera at the given location.
	 * @param target Target position
	 * @throws IllegalArgumentException if {@link #target} is the same as the current position of the camera
	 * @throws IllegalStateException if the resultant direction would result in gimbal lock
	 */
	public void look(Point target) {
		if(position.equals(target)) {
			throw new IllegalArgumentException("Cannot point camera at its current position");
		}
		final Vector look = Vector.between(target, position);
		direction(new Normal(look));
	}

	/**
	 * Sets the up axis of this camera.
	 * The default is {@link Axis#Y}.
	 * @param up Camera up axis
	 * @throws IllegalStateException if {@link #up} would result in gimbal lock
	 */
	public void up(Normal up) {
		validate(direction, up);
		this.up = requireNonNull(up);
		dirty();
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
		return right;
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
	 * @throws IllegalStateException if the camera would be gimbal locked
	 */
	private void validate(Vector direction, Vector up) {
		if(direction.equals(up) || direction.equals(up.invert())) {
			throw new IllegalStateException("Camera gimbal lock: direction=%s up=%s camera=%s".formatted(direction, up, this));
		}
	}

	/**
	 * Updates the camera axes and matrix.
	 */
	protected void update() {
		// Determine right axis
		right = new Normal(up.cross(direction));

		// Determine up axis
		final Vector y = direction.cross(right).normalize();

		// Build translation component
		final Matrix trans = Transform.translation(new Vector(position).invert());

		// Build rotation component
		final Matrix rot = new Matrix.Builder(4)
				.identity()
				.row(0, right)
				.row(1, y)
				.row(2, direction)
				.build();

		// Create camera matrix
		matrix = rot.multiply(trans);
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
		return String.format("Camera[position=%s direction=%s up=%s]", position, direction, up);
	}
}
