package org.sarge.jove.geometry;

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
	 * Creates a rotation instance.
	 * @param axis		Axis
	 * @param angle		Rotation angle (radians)
	 * @return New rotation
	 */
	static Rotation of(Vector axis, float angle) {
		return new Rotation() {
			private final Matrix matrix = Rotation.matrix(axis, angle);

			@Override
			public Vector axis() {
				return axis;
			}

			@Override
			public float angle() {
				return angle;
			}

			@Override
			public Matrix matrix() {
				return matrix;
			}

			@Override
			public int hashCode() {
				return Objects.hash(axis, angle);
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
				return new ToStringBuilder(this).append(axis).append(angle).build();
			}
		};
	}

	/**
	 * Creates a matrix for the given rotation.
	 * @param axis		Axis
	 * @param angle		Rotation angle (radians)
	 * @return New rotation matrix
	 * @throws UnsupportedOperationException for an <i>arbitrary</i> axis
	 * @see Quaternion#of(Rotation)
	 */
	static Matrix matrix(Vector axis, float angle) {
		final Builder matrix = new Matrix.Builder().identity();
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
