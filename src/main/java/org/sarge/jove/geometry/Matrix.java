package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.FloatBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>matrix</i> is a 2D square array used for geometry transformation and projection.
 * <p>
 * Notes:
 * <ul>
 * <li>The {@link #buffer(FloatBuffer)} operation buffers the matrix contents in <i>column major</i> order</li>
 * </ul>
 * @author Sarge
 */
// TODO
// - determinant, invert, etc
// - consider single array column-major
// http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
public final class Matrix implements Transform, Bufferable {
	private static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Default order size for a matrix.
	 */
	public static final int DEFAULT_ORDER = 4;

	/**
	 * Identity for an order four matrix.
	 */
	public static final Matrix IDENTITY = identity(DEFAULT_ORDER);

	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	public static Matrix identity(int order) {
		return new Builder(order).identity().build();
	}

	/**
	 * Creates a translation matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		return new Builder().identity().column(3, vec).build();
	}

	/**
	 * Creates a scaling matrix.
	 * @param scale Scaling tuple
	 * @return Scaling matrix
	 */
	public static Matrix scale(Tuple scale) {
		return new Builder()
			.identity()
			.set(0, 0, scale.x)
			.set(1, 1, scale.y)
			.set(2, 2, scale.z)
			.build();
	}

	/**
	 * Creates a <b>clockwise</b> rotation matrix about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return Rotation matrix
	 * @throws UnsupportedOperationException if the given axis is an arbitrary vector (i.e. not one of the 3 orthogonal axes)
	 */
	public static Matrix rotation(Vector axis, float angle) {
		final Builder rot = new Builder().identity();
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
		}
		return rot.build();
	}

	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param matrix 2D matrix array
	 * @throws IllegalArgumentException if the given array is not square
	 */
	public Matrix(float[][] matrix) {
		final int order = matrix.length;
		if(order < 1) throw new IllegalArgumentException("Matrix order must be one-or-more");
		this.matrix = new float[order][order];
		for(int r = 0; r < order; ++r) {
			if(matrix[r].length != order) throw new IllegalArgumentException("Invalid matrix dimensions");
			for(int c = 0; c < order; ++c) {
				this.matrix[r][c] = matrix[r][c];
			}
		}
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int order() {
		return matrix.length;
	}

	@Override
	public Matrix matrix() {
		return this;
	}

	@Override
	public int size() {
		return matrix.length * matrix.length;
	}

	@Override
	public void buffer(FloatBuffer buffer) {
		for(int c = 0; c < matrix.length; ++c) {
			for(int r = 0; r < matrix.length; ++r) {
				buffer.put(matrix[r][c]);
			}
		}
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 */
	public float get(int row, int col) {
		return matrix[row][col];
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = order();
		final Builder transpose = new Builder(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				transpose.set(c, r, matrix[r][c]);
			}
		}
		return transpose.build();
	}

	/**
	 * Multiplies two matrices.
	 * @param m Matrix
	 * @return New matrix
	 */
	public Matrix multiply(Matrix m) {
		final int order = order();
		if(m.order() != order) throw new IllegalArgumentException("Cannot multiply matrices with different sizes");
		final float[][] result = new float[order][order];
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total += matrix[r][n] * m.matrix[n][c];
				}
				result[r][c] = total;
			}
		}
		return new Matrix(result);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof Matrix) {
			final Matrix that = (Matrix) obj;
			final int order = this.order();
			if(order != that.order()) return false;
			for(int r = 0; r < order; ++r) {
				for(int c = 0; c < order; ++c) {
					if(!MathsUtil.equals(matrix[r][c], that.matrix[r][c])) return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int order = order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				if(c > 0) {
					sb.append(",");
				}
				sb.append(String.format("%10.5f", matrix[r][c]));
			}
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Builder for a matrix.
	 */
	public static class Builder {
		private final float[][] matrix;
		private final int order;

		/**
		 * Constructor for a matrix of the given order.
		 * @param order Matrix order
		 */
		public Builder(int order) {
			this.order = oneOrMore(order);
			this.matrix = new float[order][order];
		}

		/**
		 * Constructor for a matrix with an order {@link Matrix#DEFAULT_ORDER}.
		 */
		public Builder() {
			this(DEFAULT_ORDER);
		}

		/**
		 * Initialises this matrix to the identity matrix.
		 * Invoking this method on a builder that has already been mutated is undefined, i.e. this method should be invoked <b>first</b> if required.
		 */
		public Builder identity() {
			for(int n = 0; n < order; ++n) {
				matrix[n][n] = 1;
			}
			return this;
		}

		/**
		 * Sets a matrix element.
		 * @param row 		Row
		 * @param col		Column
		 * @param value		Matrix element
		 */
		public Builder set(int row, int col, float value) {
			matrix[row][col] = value;
			return this;
		}

		/**
		 * Sets a matrix row to the given vector.
		 * @param row Row index
		 * @param vec Vector
		 */
		public Builder row(int row, Tuple vec) {
			matrix[row][0] = vec.x;
			matrix[row][1] = vec.y;
			matrix[row][2] = vec.z;
			return this;
		}

		/**
		 * Sets a matrix column to the given vector.
		 * @param col Column index
		 * @param vec Vector
		 */
		public Builder column(int col, Tuple vec) {
			matrix[0][col] = vec.x;
			matrix[1][col] = vec.y;
			matrix[2][col] = vec.z;
			return this;
		}

		/**
		 * Constructs this matrix.
		 * @return New matrix
		 */
		public Matrix build() {
			return new Matrix(matrix);
		}
	}
}
