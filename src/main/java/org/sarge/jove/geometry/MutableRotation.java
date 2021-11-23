package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>mutable rotation</i> specifies a counter-clockwise rotation about an axis.
 * TODO - doc mapper
 * @author Sarge
 */
public class MutableRotation implements Transform, Rotation {
	/**
	 * Helper - Creates a rotation with an arbitrary axis based on a quaternion.
	 * @param axis Rotation axis
	 * @return Quaternion based rotation
	 * @see Quaternion#of(Rotation)
	 */
	public static MutableRotation quaternion(Vector axis) {
		return new MutableRotation(axis, Quaternion::of);
	}

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
