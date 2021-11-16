package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> is a counter-clockwise rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Rotation angle (radians)
	 */
	float angle();

	/**
	 * Creates a matrix for a <b>counter clockwise</b> rotation about the given pre-defined axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return New rotation matrix
	 * @throws UnsupportedOperationException if the axis is not pre-defined
	 * @see Matrix
	 * @see Quaternion#of(Vector, float)
	 */
	static Matrix matrix(Vector axis, float angle) {
		final Builder rot = new Builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		if(Vector.X == axis) {
			rot.set(1, 1, cos);
			rot.set(1, 2, -sin);
			rot.set(2, 1, sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Y == axis) {
			rot.set(0, 0, cos);
			rot.set(0, 2, sin);
			rot.set(2, 0, -sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Z == axis) {
			rot.set(0, 0, cos);
			rot.set(0, 1, -sin);
			rot.set(1, 0, sin);
			rot.set(1, 1, cos);
		}
		else {
			throw new UnsupportedOperationException("Arbitrary axis not supported (use quaternion)");
		}
		return rot.build();
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractRotation implements Rotation {
		protected final Vector axis;
		protected float angle;

		/**
		 * Constructor.
		 * @param axis		Rotation axis
		 * @param angle		Rotation angle (radians)
		 */
		protected AbstractRotation(Vector axis, float angle) {
			this.axis = notNull(axis);
			this.angle = angle;
		}

		@Override
		public final Vector axis() {
			return axis;
		}

		@Override
		public final float angle() {
			return angle;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Rotation that) &&
					this.axis.equals(that.axis()) &&
					MathsUtil.isEqual(this.angle, that.angle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(axis).append(angle).toString();
		}
	}
}
