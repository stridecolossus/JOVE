package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * <p>
 * The rotation matrix is constructed by the {@link #build()} method which delegates to {@link Quaternion#of(Rotation)} by default.
 * <p>
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
		return build();
	}

	/**
	 * Constructs the rotation matrix.
	 * @return Rotation matrix
	 */
	protected Matrix build() {
		return Quaternion.of(this).matrix();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof MutableRotation that) &&
				this.axis.equals(that.axis()) &&
				MathsUtil.isEqual(this.angle, that.angle());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(axis).append(angle).build();
	}
}
