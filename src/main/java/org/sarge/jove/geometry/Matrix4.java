package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>Matrix4</i> is a 4-by-4 matrix implementation that also provides additional factory methods for common use-cases such as rotation matrices.
 * @author Sarge
 */
public final class Matrix4 extends Matrix {
	/**
	 * Order for a 4x4 matrix.
	 */
	public static final int ORDER = 4;

	/**
	 * Identity matrix.
	 */
	public static final Matrix IDENTITY = Matrix.identity(ORDER);

	/**
	 * @return New builder for a 4x4 matrix
	 */
	public static Builder builder() {
		return new Builder(ORDER);
	}

	/**
	 * Creates a translation matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		return builder()
				.identity()
				.column(3, vec)
				.build();
	}

	/**
	 * Creates a scaling matrix.
	 * @return Scaling matrix
	 */
	public static Matrix scale(float x, float y, float z) {
		return builder()
				.identity()
				.set(0, 0, x)
				.set(1, 1, y)
				.set(2, 2, z)
				.build();
	}

	/**
	 * Creates a scaling matrix.
	 * @param scale Scaling tuple (for all three directions)
	 * @return Scaling matrix
	 */
	public static Matrix scale(float scale) {
		return scale(scale, scale, scale);
	}

	/**
	 * Creates a <b>clockwise</b> rotation matrix about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return Rotation matrix
	 * @throws UnsupportedOperationException if the given axis is an arbitrary vector (i.e. not one of the 3 orthogonal axes)
	 */
	public static Matrix rotation(Vector axis, float angle) { // TODO - Rotation? move to that class?
		final Builder rot = builder().identity();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);
		if(Vector.X.equals(axis)) {
			rot.set(1, 1, cos);
			rot.set(1, 2, sin);
			rot.set(2, 1, -sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Y.equals(axis)) {
			rot.set(0, 0, cos);
			rot.set(0, 2, -sin);
			rot.set(2, 0, sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Z.equals(axis)) {
			rot.set(0, 0, cos);
			rot.set(0, 1, -sin);
			rot.set(1, 0, sin);
			rot.set(1, 1, cos);
		}
		else {
			throw new UnsupportedOperationException("Arbitrary rotation axis not supported");
			// TODO - return Quaternion(rotation)?
		}
		return rot.build();
	}

	@Override
	public int order() {
		return ORDER;
	}

	protected Matrix4(float[] matrix) {
		super(matrix);
	}
}
