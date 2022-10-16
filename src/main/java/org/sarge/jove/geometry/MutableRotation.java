package org.sarge.jove.geometry;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableRotation extends AxisAngle {
	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		super(axis, 0);
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void angle(float angle) {
		this.angle = angle;
	}

	@Override
	public boolean isMutable() {
		return true;
	}
}
// TODO - factory method on rotation?
