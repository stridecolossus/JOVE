package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * @see Quaternion#of(Rotation)
 * @author Sarge
 */
public class MutableRotation implements Rotation {
	private final Vector axis;
	private float angle;
	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		this.axis = notNull(axis);
	}

	@Override
	public Vector axis() {
		return axis;
	}

	@Override
	public float angle() {
		return angle;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(axis).append(angle).toString();
	}
}
