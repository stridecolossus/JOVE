package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>matrix</i> is a 2-dimensional floating-point array used for geometry transformation and projection.
 * <p>
 * Matrix properties:
 * <ul>
 * <li>Matrices are constrained to be <i>square</i>, i.e. same width and height</li>
 * <li>The <i>order</i> specifies the dimensions of the matrix</li>
 * <li>Matrix data is in <i>column major</i> order (the Vulkan default)</li>
 * <li>Matrices are also {@link Bufferable}</li>
 * </ul>
 * <p>
 * In general matrices are constructed using the builder:
 * <p>
 * <pre>
 * Matrix matrix = new Matrix.Builder()
 * 	.identity()
 * 	.set(row, col, value)
 * 	.build();
 * </pre>
 * @author Sarge
 */
public final class Matrix implements Transform, Bufferable {
	private static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Order for a 4x4 matrix.
	 */
	private static final int DEFAULT_ORDER = 4;

	/**
	 * 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = Matrix.identity(DEFAULT_ORDER);

	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	public static Matrix identity(int order) {
		return new Builder(order).identity().build();
	}

	/**
	 * Creates a 4x4 translation matrix.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		return new Builder()
				.identity()
				.column(3, vec)
				.build();
	}

	/**
	 * Creates a 4x4 scaling matrix.
	 * @return Scaling matrix
	 */
	public static Matrix scale(float x, float y, float z) {
		return new Builder()
				.identity()
				.set(0, 0, x)
				.set(1, 1, y)
				.set(2, 2, z)
				.build();
	}

	/**
	 * Creates a 4x4 <b>clockwise</b> rotation matrix about the given axis.
	 * @param axis		Rotation axis
	 * @param angle		Angle (radians)
	 * @return Rotation matrix
	 * @throws UnsupportedOperationException if the given axis is an arbitrary vector (i.e. not one of the 3 orthogonal axes)
	 */
	public static Matrix rotation(Vector axis, float angle) { // TODO - Rotation? move to that class?
		final Builder rot = new Builder().identity();
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

	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param order Matrix order
	 */
	private Matrix(int order) {
		matrix = new float[order][order];
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int order() {
		return matrix.length;
	}

	@Override
	public final Matrix matrix() {
		return this;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 * @throws ArrayIndexOutOfBoundsException if the row or column are out-of-bounds
	 */
	public float get(int row, int col) {
		return matrix[row][col];
	}

	/**
	 * Extracts a matrix row as a vector.
	 * @param row Row index
	 * @return Matrix row
	 * @throws ArrayIndexOutOfBoundsException if the row index is invalid or the matrix is too small
	 */
	public Vector row(int row) {
		final float x = matrix[row][0];
		final float y = matrix[row][1];
		final float z = matrix[row][2];
		return new Vector(x, y, z);
	}

	/**
	 * Extracts a matrix column as a vector.
	 * @param col Column index
	 * @return Matrix column
	 * @throws ArrayIndexOutOfBoundsException if the column index is invalid or the matrix is too small
	 */
	public Vector column(int col) {
		final float x = matrix[0][col];
		final float y = matrix[1][col];
		final float z = matrix[2][col];
		return new Vector(x, y, z);
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		final int order = order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				buffer.putFloat(matrix[c][r]);
			}
		}
	}

	@Override
	public int length() {
		return matrix.length * matrix.length * Float.BYTES;
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = order();
		final Matrix trans = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				trans.matrix[r][c] = this.matrix[c][r];
			}
		}
		return trans;
	}

	/**
	 * Multiplies this and the given matrix.
	 * @param m Matrix
	 * @return New matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	public Matrix multiply(Matrix m) {
		// Check same sized matrices
		final int order = order();
		if(m.order() != order) throw new IllegalArgumentException("Cannot multiply matrices with different sizes");

		// Multiply matrices
		final Matrix result = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total += this.matrix[r][n] * m.matrix[n][c];
				}
				result.matrix[r][c] = total;
			}
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Matrix that) &&
				(this.order() == that.order()) &&
				Arrays.deepEquals(this.matrix, that.matrix);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int order = this.order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				sb.append(String.format("%10.5f ", matrix[r][c]));
			}
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Builder for a matrix.
	 */
	public static class Builder {
		private Matrix matrix;

		/**
		 * Default constructor for a 4x4 matrix builder.
		 */
		public Builder() {
			this(DEFAULT_ORDER);
		}

		/**
		 * Constructor for a matrix builder of the given order.
		 * @param order Matrix order
		 * @throws IllegalArgumentException for an illogical matrix order
		 */
		public Builder(int order) {
			Check.oneOrMore(order);
			matrix = new Matrix(order);
		}

		/**
		 * Initialises this matrix to identity.
		 */
		public Builder identity() {
			final int order = matrix.order();
			for(int n = 0; n < order; ++n) {
				set(n, n, 1);
			}
			return this;
		}

		/**
		 * Sets a matrix element.
		 * @param row 		Row
		 * @param col		Column
		 * @param value		Matrix element
		 * @throws ArrayIndexOutOfBoundsException if the row or column is out-of-bounds
		 */
		public Builder set(int row, int col, float value) {
			matrix.matrix[row][col] = value;
			return this;
		}

		/**
		 * Sets a matrix row to the given vector.
		 * @param row		Row index
		 * @param vec 		Vector
		 * @throws ArrayIndexOutOfBoundsException if the row is out-of-bounds
		 */
		public Builder row(int row, Tuple vec) {
			set(row, 0, vec.x);
			set(row, 1, vec.y);
			set(row, 2, vec.z);
			return this;
		}

		/**
		 * Sets a matrix column to the given vector.
		 * @param col 		Column index
		 * @param vec		Vector
		 * @throws ArrayIndexOutOfBoundsException if the column is out-of-bounds
		 */
		public Builder column(int col, Tuple vec) {
			set(0, col, vec.x);
			set(1, col, vec.y);
			set(2, col, vec.z);
			return this;
		}

		/**
		 * Constructs this matrix.
		 * @return New matrix
		 */
		public Matrix build() {
			try {
				return matrix;
			}
			finally {
				matrix = null;
			}
		}
	}
}
