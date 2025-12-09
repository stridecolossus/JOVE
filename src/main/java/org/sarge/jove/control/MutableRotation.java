package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.control.Animator.Animation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>mutable rotation</i> is a mutable adapter for an axis-angle.
 * @author Sarge
 */
public class MutableRotation implements Rotation {
	private AxisAngle rotation;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Normal axis) {
		set(axis, 0);
	}

	/**
	 * Constructor given an axis-angle.
	 * @param rotation Axis-angle
	 */
	public MutableRotation(AxisAngle rotation) {
		this.rotation = requireNonNull(rotation);
	}

	/**
	 * @return This rotation as an animation about the unit-circle
	 */
	public Animation animation() {
		return pos -> set(pos * MathsUtility.TWO_PI);
	}

	@Override
	public AxisAngle toAxisAngle() {
		return rotation;
	}

	@Override
	public Matrix matrix() {
		return rotation.matrix();
	}

	/**
	 * Sets the rotation axis.
	 * @param axis Rotation axis
	 */
	public void set(Axis axis) {
		set(axis, rotation.angle());
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void set(float angle) {
		set(rotation.axis(), angle);
	}

	/**
	 * Sets this rotation.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 */
	public void set(Normal axis, float angle) {
		rotation = new AxisAngle(axis, angle);
	}

	@Override
	public Vector rotate(Vector vector) {
		return rotation.rotate(vector);
	}

	@Override
	public int hashCode() {
		return rotation.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Rotation that) &&
				this.rotation.equals(that.toAxisAngle());
	}

	@Override
	public String toString() {
		return String.format("MutableRotation[%s]", rotation);
	}
}
