package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>camera</i> represents a viewers position and orientation.
 * @author Sarge
 */
public class Camera {
	// Camera state
	private Point pos = Point.ORIGIN;
	private Vector dir = Vector.Z_AXIS;			// Note is actually inverse of the view direction

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
		move(dir.scale(dist));
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
		return dir.invert();
	}

	/**
	 * Sets the camera view direction.
	 * @param dir View direction (assumes normalized)
	 */
	public void direction(Vector dir) {
		this.dir = dir.invert();
		dirty();
	}

	/**
	 * Helper - Points the camera at the given location.
	 * @param pt Camera point-of-interest
	 * @throws IllegalArgumentException if the location is the same as the current position of the camera
	 */
	public void look(Point pt) {
		if(pos.equals(pt)) throw new IllegalArgumentException("Cannot point camera at its current position");
		dir = Vector.between(pt, pos).normalize();
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

//	/**
//	 * Rotates the camera.
//	 * @param rot Rotation
//	 * @see Quaternion#rotate(Vector)
//	 */
//	public void rotate(Rotation rot) {
//		// TODO
//		dir = Quaternion.of(rot).rotate(dir);
//		dirty();
//	}

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
		right = up.cross(dir).normalize();

		// Determine up axis
		final Vector y = dir.cross(right).normalize();

		// Calculate translation component
		final Matrix trans = Matrix.translation(pos.toVector().invert());

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
}
