package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * <p>
 * Calculation of the resultant rotation matrix is delegated to a <i>mapper</i> function which is {@link Quaternion#of(Rotation)} by default.
 * <p>
 * @see Quaternion#of(Rotation)
 * @see Rotation#matrix(Rotation)
 * @author Sarge
 */
public class MutableRotation implements Transform, Rotation {
	private final Vector axis;
	private final Function<Rotation, Transform> mapper;
	private float angle;
	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param axis			Rotation axis
	 * @param mapper		Matrix mapper
	 */
	public MutableRotation(Vector axis, Function<Rotation, Transform> mapper) {
		this.axis = notNull(axis);
		this.mapper = notNull(mapper);
	}

	/**
	 * Constructor for a rotation based on a quaternion.
	 * @param axis Rotation axis
	 */
	public MutableRotation(Vector axis) {
		this(axis, Quaternion::of);
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
		return mapper.apply(this).matrix();
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
