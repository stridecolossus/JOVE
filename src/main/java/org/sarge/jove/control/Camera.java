package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

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
	private Point pos = Point.ORIGIN;
	private Normal dir = Axis.Z;
	private Normal up = Axis.Y;

	// Transient view transform
	private Normal right = Axis.X;
	private Matrix matrix;
	private boolean dirty = true;

	/**
	 * @return Camera position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Moves the camera to a new position.
	 * @param position New position
	 */
	public void move(Point position) {
		this.pos = requireNonNull(position);
		dirty();
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vector Movement vector
	 */
	public void move(Vector vector) {
		pos = pos.add(vector);
		dirty();
	}

	/**
	 * Moves the camera by the given distance in the current view direction.
	 * @param distance Distance to move
	 * @see #direction()
	 */
	public void move(float distance) {
		move(dir.multiply(distance));
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
		return dir;
	}

	/**
	 * Sets the camera view direction.
	 * @param dir View direction
	 * @throws IllegalStateException if the direction would result in gimbal lock
	 */
	public void direction(Normal dir) {
		validate(dir, up);
		this.dir = requireNonNull(dir);
		dirty();
	}

	/**
	 * Points the camera at the given location.
	 * @param target Target position
	 * @throws IllegalArgumentException if {@link #target} is the same as the current position of the camera
	 * @throws IllegalStateException if the resultant direction would result in gimbal lock
	 */
	public void look(Point target) {
		if(pos.equals(target)) throw new IllegalArgumentException("Cannot point camera at its current position");
		final Vector look = Vector.between(target, pos);
		direction(new Normal(look));
	}

	/**
	 * Sets the up axis of this camera.
	 * The default is {@link Axis#Y}.
	 * @param up Camera up axis
	 * @throws IllegalStateException if {@link #up} would result in gimbal lock
	 */
	public void up(Normal up) {
		validate(dir, up);
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
	private void validate(Vector dir, Vector up) {
		if(dir.equals(up) || dir.equals(up.invert())) {
			throw new IllegalStateException("Camera gimbal lock: dir=%s up=%s this=%s".formatted(dir, up, this));
		}
	}

	/**
	 * Updates the camera axes and matrix.
	 */
	protected void update() {
		// Determine right axis
		right = new Normal(up.cross(dir));

		// Determine up axis
		final Vector y = dir.cross(right).normalize();

		// Build translation component
		final Matrix trans = Transform.translation(new Vector(pos).invert());

		// Build rotation component
		final Matrix rot = new Matrix.Builder(4)
				.identity()
				.row(0, right)
				.row(1, y)
				.row(2, dir)
				.build();

		// Create camera matrix
		matrix = rot.multiply(trans);
	}
}
