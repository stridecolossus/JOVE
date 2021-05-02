package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>Matrix4</i> is a 4-by-4 matrix implementation that also provides additional factory methods for common cases such as creating rotation matrices.
 * @author Sarge
 */
public final class Matrix4 extends DefaultMatrix {
	/**
	 * Order for for a 4x4 matrix.
	 */
	public static final int SIZE = 4;

	/**
	 * Identity matrix.
	 */
	public static final Matrix IDENTITY = Matrix.identity(SIZE);

	/**
	 * @return New builder for a 4x4 matrix
	 */
	public static Builder builder() {
		return new Builder(SIZE);
	}

	/**
	 * Creates a translation matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		return builder().identity().column(3, vec).build();
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
		if(Vector.X_AXIS.equals(axis)) {
			rot.set(1, 1, cos);
			rot.set(1, 2, sin);
			rot.set(2, 1, -sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Y_AXIS.equals(axis)) {
			rot.set(0, 0, cos);
			rot.set(0, 2, -sin);
			rot.set(2, 0, sin);
			rot.set(2, 2, cos);
		}
		else
		if(Vector.Z_AXIS.equals(axis)) {
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

	/**
	 * Constructor.
	 * @param matrix Column-major matrix elements
	 * @throws IllegalArgumentException if the matrix is not square or the array length does not match the matrix order
	 */
	public Matrix4(float[] matrix) {
		super(matrix);
	}

	@Override
	public int order() {
		return SIZE;
	}

	@Override
	protected Matrix create(float[] matrix) {
		assert matrix.length == SIZE * SIZE;
		return new Matrix4(matrix);
	}
}
