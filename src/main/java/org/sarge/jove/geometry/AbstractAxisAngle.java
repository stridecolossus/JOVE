package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;

/**
 * Skeleton implementation.
 * @author Sarge
 */
abstract class AbstractAxisAngle implements AxisAngle {
	private final Normal axis;

	/**
	 * Constructor.
	 * @param axis Rotation axis
	 */
	protected AbstractAxisAngle(Normal axis) {
		this.axis = notNull(axis);
	}

	@Override
	public final Normal axis() {
		return axis;
	}

	@Override
	public final AxisAngle toAxisAngle() {
		return this;
	}

	/**
	 * Rotates the given vector by this axis-angle.
	 * <p>
	 * This method uses <i>Rodrigues' rotation formula</i> which is expressed as:
	 * <pre>rot = cos(a)v + sin(a)(n x v) + (1 - cos(a))(n.v)n</pre>
	 * Where:
	 * <ul>
	 * <li><i>a</i> is the rotation angle</li>
	 * <li><i>n</i> is the axis (or normal)</li>
	 * <li>and <i>v</i> is the vector to be rotated</li>
	 * </ul>
	 * <p>
	 * This approach may be more efficient than constructing a rotation matrix or quaternion for certain use-cases.
	 */
	@Override
	public Vector rotate(Vector vec) {
		final float angle = this.angle();
		final float cos = MathsUtil.cos(angle);
		final Vector a = vec.multiply(cos);
		final Vector b = axis.cross(vec).multiply(MathsUtil.sin(angle));
		final Vector c = axis.multiply((1 - cos) * axis.dot(vec));
		return a.add(b).add(c);
	}

	@Override
	public int hashCode() {
		return Objects.hash(axis, angle());
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractAxisAngle that) &&
				this.axis.equals(that.axis) &&
				MathsUtil.isEqual(this.angle(), that.angle());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(axis)
				.append(angle())
				.build();
	}
}
