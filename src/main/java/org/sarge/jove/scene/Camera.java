package org.sarge.jove.scene;

import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MatrixBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Quaternion;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Camera position and view.
 * @author Sarge
 */
public class Camera {
	// Camera model
	private Point pos = new Point(0, 0, 5);
	private Point target = Point.ORIGIN;
	private Vector dir = Vector.Z_AXIS.invert();

	// Axes
	private Vector up = Vector.Y_AXIS;
	private Vector right = new Vector();

	// Matrix
	private final MatrixBuilder builder = new MatrixBuilder();
	private Matrix matrix;
	private boolean dirty;

	/**
	 * Constructor.
	 */
	public Camera() {
		update();
	}

	/**
	 * @return Camera location
	 */
	public Point getPosition() {
		return pos;
	}

	/**
	 * Sets the location of the camera.
	 * @param pos
	 */
	public void setPosition(Point pos) {
		Check.notNull(pos);
		this.pos = pos;
		updateDirection();
	}

	/**
	 * @return Camera target position
	 */
	public Point getTarget() {
		return target;
	}

	/**
	 * Sets the camera target.
	 * @param target Camera target
	 */
	public void setTarget(Point target) {
		Check.notNull(target);
		this.target = target;
		updateDirection();
	}

	/**
	 * @return Camera view direction
	 */
	public Vector getDirection() {
		return dir;
	}

	/**
	 * @return Up axis
	 */
	public Vector getUpDirection() {
		return up;
	}

	/**
	 * Sets the camera up direction.
	 * @param up
	 */
	public void setUpDirection(Vector up) {
		Check.notNull(up);
		this.up = up.normalize();
		dirty = true;
	}

	/**
	 * @return Right axis of this camera
	 */
	public Vector getRightAxis() {
		return right;
	}

	/**
	 * @return View matrix for this camera
	 */
	public Matrix getViewMatrix() {
		if(dirty) {
			update();
			dirty = false;
		}

		return matrix;
	}

	/**
	 * Moves the camera position in the current direction.
	 * @param amount Movement amount
	 * @see #getDirection()
	 * @see #move(Vector)
	 */
	public void move(float amount) {
		move(dir.multiply(amount));
	}

	/**
	 * Helper - Strafes the camera position (positive is in the direction of the <b>right</b> axis).
	 * @param amount
	 * @see #getRightAxis()
	 * @see #move(Vector)
	 */
	public void strafe(float amount) {
		move(right.multiply(amount));
	}

	/**
	 * Moves the camera by the given vector.
	 * @param vec Movement vector
	 */
	public void move(Vector vec) {
		pos.add(vec);
		target.add(vec);
		dirty = true;
	}

	/**
	 * Rotates this camera counter-clockwise about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public void rotate(Quaternion rot) {
		rot.rotate(dir);
		// TODO - calc new target position
		// TODO - rotate the target position and re-calc direction via updateDirection()
		dirty = true;
	}

	/**
	 * Orbits the camera by the given rotation about the target position.
	 * @param rot Rotation
	 */
	public void orbit(Quaternion rot) {
		// TODO
		/*
		// TODO - document and test

		trans.subtract(target, pos);
		pos.set(target);
		rot.rotate(trans);

		pos.add(trans);
		dir.subtract(pos, target).normalize();

		dirty = true;
		*/
	}

	/**
	 * Updates the camera axes and view matrix after a change in position or orientation.
	 */
	private void update() {
		// Derive camera axes
		final Vector z = dir.invert();
		this.right = up.cross(z).normalize();
		final Vector y = z.cross(right).normalize();
		
		// Calculate camera translation
		final Vector trans = new Vector(
			right.dot(pos),
			y.dot(pos),
			z.dot(pos)
		);

		// Construct camera matrix
		matrix = builder
			.identity()
			.setRow(0, right)
			.setRow(1, y)
			.setRow(2, z)
			.setColumn(3, trans.invert())
			.build();
	}

	/**
	 * Updates camera direction.
	 */
	private void updateDirection() {
		if(pos.equals(target)) throw new IllegalArgumentException("Camera position cannot be same as its target");
		dir = Vector.between(pos, target).normalize();
		dirty = true;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
