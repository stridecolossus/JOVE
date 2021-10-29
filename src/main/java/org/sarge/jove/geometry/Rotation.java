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
	 * Creates a matrix for a <b>clockwise</b> rotation about the given pre-defined axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return New rotation matrix
	 * @throws UnsupportedOperationException if the axis is not pre-defined
	 * @see Quaternion#of(Vector, float)
	 */
	static Matrix matrix(Vector axis, float angle) {
		final Builder rot = new Builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		// TODO - do we use these 2x2 matrices elsewhere? i.e. factor out 2x2 and introduce set(r,c,2x2)?
		if(Vector.X == axis) {
			rot.set(1, 1, cos);
			rot.set(1, 2, sin);
			rot.set(2, 1, -sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Y == axis) {
			rot.set(0, 0, cos);
			rot.set(0, 2, -sin);
			rot.set(2, 0, sin);
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

	/**
	 * Mutable implementation.
	 */
	class MutableRotation extends AbstractRotation {
		/**
		 * Helper - Defines the rotation factory method.
		 */
		@FunctionalInterface
		private static interface Factory {
			Transform matrix(Vector axis, float angle);
		}

		private final Factory factory;
		private boolean dirty = true;

		/**
		 * Constructor.
		 * @param axis Rotation axis
		 */
		public MutableRotation(Vector axis) {
			super(axis, 0);
			this.factory = isAxis(axis) ? Rotation::matrix : Quaternion::of;
		}

		/**
		 * @return Whether the given vector is a pre-defined or arbitrary axis
		 */
		private static boolean isAxis(Vector vec) {
			return (vec == Vector.X) || (vec == Vector.Y) || (vec == Vector.Z);
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
			return factory.matrix(axis, angle).matrix();
		}
	}
}
