package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix.Builder;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return Rotation axis
	 */
	Vector axis();

	/**
	 * @return Counter-clockwise rotation angle (radians)
	 */
	float angle();

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractRotation implements Rotation {
		private final Vector axis;

		/**
		 * Constructor.
		 * @param axis Rotation axis
		 */
		protected AbstractRotation(Vector axis) {
			this.axis = notNull(axis);
		}

		@Override
		public final Vector axis() {
			return axis;
		}

		@Override
		public int hashCode() {
			return Objects.hash(axis, angle());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Rotation that) &&
					this.axis().equals(that.axis()) &&
					MathsUtil.isEqual(this.angle(), that.angle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(axis).append(angle()).build();
		}
	}

	/**
	 * Creates a rotation instance.
	 * @param axis		Axis
	 * @param angle		Rotation angle (radians)
	 * @return New rotation
	 */
	static Rotation of(Vector axis, float angle) {
		return new AbstractRotation(axis) {
			private final Matrix matrix = super.matrix();

			@Override
			public float angle() {
				return angle;
			}

			@Override
			public Matrix matrix() {
				return matrix;
			}
		};
	}

	/**
	 * @return Rotation matrix
	 * @throws UnsupportedOperationException for an <i>arbitrary</i> axis
	 * @see Quaternion#of(Rotation)
	 */
	@Override
	default Matrix matrix() {
		final Builder matrix = new Matrix.Builder().identity();
		final Vector axis = this.axis();
		final float angle = this.angle();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		if(Vector.X.equals(axis)) {
			matrix.set(1, 1, cos);
			matrix.set(1, 2, -sin);
			matrix.set(2, 1, sin);
			matrix.set(2, 2, cos);
		}
		else
		if(Vector.Y.equals(axis)) {
			matrix.set(0, 0, cos);
			matrix.set(0, 2, sin);
			matrix.set(2, 0, -sin);
			matrix.set(2, 2, cos);
		}
		else
		if(Vector.Z.equals(axis)) {
			matrix.set(0, 0, cos);
			matrix.set(0, 1, -sin);
			matrix.set(1, 0, sin);
			matrix.set(1, 1, cos);
		}
		else {
			throw new UnsupportedOperationException("Arbitrary axis not supported (use quaternion)");
		}
		return matrix.build();
	}
}
