package org.sarge.jove.geometry;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * @see AxisAngle
 * @author Sarge
 */
public class MutableRotation implements Rotation {
	private final Vector axis;
	private float angle;
	private AxisAngle rot;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		this.axis = axis.normalize();
	}

	/**
	 * @return Rotation axis
	 */
	public Vector axis() {
		return axis;
	}

	/**
	 * @return Rotation angle (radians)
	 */
	public float angle() {
		return angle;
	}

	/**
	 * Sets the rotation angle.
	 * @param angle Rotation angle (radians)
	 */
	public void angle(float angle) {
		this.angle = angle;
		rot = null;
	}

	@Override
	public AxisAngle toAxisAngle() {
		update();
		return rot;
	}

	@Override
	public boolean isDirty() {
		return rot == null;
	}

	@Override
	public Matrix matrix() {
		update();
		return rot.matrix();
	}

	@Override
	public Vector rotate(Vector vec) {
		update();
		return rot.rotate(vec);
	}

	private void update() {
		rot = new AxisAngle(axis, angle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(axis).append(angle).build();
	}
}
