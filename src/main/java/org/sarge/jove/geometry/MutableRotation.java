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
	public MutableRotation(Normal axis) {
		super(axis, 0);
	}

	@Override
	public void set(float angle) {
		super.set(angle);
	}

	@Override
	public final boolean isMutable() {
		return true;
	}
}
