package org.sarge.jove.scene.core;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * A <i>camera</i> is a model of the viewers position and orientation, used to generate the <i>view transform</i> matrix.
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
		move(dir.multiply(dist));
	}

	/**
	 * Moves the camera by the given distance along the current right axis.
	 * @param dist Distance to strafe
	 * @see #right()
	 */
	public void strafe(float dist) {
		move(right.multiply(dist));
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
		this.dir = notNull(dir);
		dirty();
	}

	/**
	 * Helper - Points the camera at the given location.
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
	 * Sets the up axis of this camera (default is {@link Axis#Y}).
	 * @param up Camera up axis
	 * @throws IllegalStateException if {@link #up} would result in gimbal lock
	 */
	public void up(Normal up) {
		validate(dir, up);
		this.up = notNull(up);
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
	public Normal right() {
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
		right = up.cross(dir).normalize();

		// Determine up axis
		final Vector y = dir.cross(right).normalize();

		// Build translation component
		final Matrix trans = Matrix.translation(new Vector(pos).invert());

		// Build rotation component
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
}
