package org.sarge.jove.geometry;

/**
 * A <i>transform</i> is represented by a 4x4 {@link Matrix} for model transformation and projection.
 * @author Sarge
 */
public interface Transform {
	/**
	 * @return Transformation matrix
	 */
	Matrix matrix();

	/**
	 * Creates a 4x4 translation matrix by populating the top-right column of the matrix.
	 * @param vector Translation vector
	 * @return Translation matrix
	 */
	static Matrix translation(Vector vector) {
		return new Matrix.Builder()
				.identity()
				.column(3, vector)
				.build();
	}

	/**
	 * Creates a 4x4 scaling matrix by populating the diagonal of the matrix.
	 * @return Scaling matrix
	 */
	static Matrix scale(float x, float y, float z) {
		return new Matrix.Builder()
				.set(0, 0, x)
				.set(1, 1, y)
				.set(2, 2, z)
				.set(3, 3, 1)
				.build();
	}

	/**
	 * Creates a 4x4 scaling matrix by populating the diagonal of the matrix with the given scalar.
	 * @param f Scalar
	 * @return Scaling matrix
	 * @see #scale(float, float, float)
	 */
	static Matrix scale(float f) {
		return scale(f, f, f);
	}
}
