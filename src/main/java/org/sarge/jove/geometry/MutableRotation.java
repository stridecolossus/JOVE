package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Rotation.AbstractRotation;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * @see Quaternion#of(Rotation)
 * @author Sarge
 */
public class MutableRotation extends AbstractRotation {
	private boolean dirty = true;

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
		dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public Matrix matrix() {
		dirty = false;
		return Quaternion.of(this).matrix();
	}
}
