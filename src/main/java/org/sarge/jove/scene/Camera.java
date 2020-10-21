package org.sarge.jove.scene;

import static org.sarge.jove.util.Check.notNull;

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
	// http://www.songho.ca/opengl/gl_camera.html

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
	 */
	public void look(Point pt) {
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
		matrix();
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
		dir = new Vector(x, y, z).normalize();
		dirty();
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
		final Vector trans = new Vector(right.dot(pos), y.dot(pos), -dir.dot(pos));

		// Build camera matrix
		matrix = new Matrix.Builder()
			.identity()
			.row(0, right)
			.row(1, y)
			.row(2, dir.invert())
			.column(3, trans)
			.build();
	}
}
