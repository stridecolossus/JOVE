package org.sarge.jove.geometry;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
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
 * <li>Matrix data written by {@link #buffer(ByteBuffer)} is <i>column major</i> (the Vulkan default)</li>
 * </ul>
 * <p>
 * In general matrices are constructed using the builder:
 * <pre>
 * Matrix matrix = new Matrix.Builder()
 * 	.identity()
 * 	.set(row, col, value)
 * 	.build();
 * </pre>
 * <p>
 * An order 4 matrix with the following structure is used for view and perspective transformation:
 * <p>
 * <table border=0>
 * <tr><td>Rx</td><td>Ry</td><td>Rz</td><td>Tx</td></tr>
 * <tr><td>Yx</td><td>Yy</td><td>Yz</td><td>Ty</td></tr>
 * <tr><td>Dx</td><td>Dy</td><td>Dz</td><td>Tz</td></tr>
 * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
 * </table>
 * <p>
 * Where:
 * <ul>
 * <li>the top-left 3x3 component of the matrix is the view rotation and the right-hand column is the transformation</li>
 * <li>in camera terms R is the <i>right</i> vector, Y is <i>up</i> and D is the view <i>direction</i></li>
 * <li>T is the view transformation (or eye position)</li>
 * </ul>
 * Note that both components are inverted (transposed and negated) since the scene is transformed in the opposite direction to the view (or camera).
 * <p>
 * @see Rotation
 * @author Sarge
 */
public final class Matrix implements Transform, Bufferable {
	/**
	 * Order for a 4x4 matrix.
	 */
	public static final int DEFAULT_ORDER = 4;

	/**
	 * 4x4 identity matrix.
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
	 * Creates a 4x4 translation matrix by populating the top-right column of the matrix.
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
	 * Creates a 4x4 scaling matrix and populating the diagonal of the matrix.
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
	public Matrix matrix() {
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
	 * <p>
	 * Note that matrix multiplication is <b>non-commutative</b>.
	 * The resultant matrix first applies the given matrix and <b>then</b> this matrix, i.e. <code>A x B</code> applies B then A.
	 * <p>
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
				equals(that);
	}

	private boolean equals(Matrix that) {
		final int order = this.order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				if(!MathsUtil.isEqual(this.matrix[r][c], that.matrix[r][c])) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int order = this.order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				sb.append(String.format("%10.5f ", matrix[r][c]));
			}
			sb.append(StringUtils.LF);
		}
		return sb.toString();
	}

	/**
	 * Builder for a matrix.
	 */
	public static class Builder {
		private Matrix matrix;

		/**
		 * Default constructor for a 4x4 matrix.
		 */
		public Builder() {
			this(DEFAULT_ORDER);
		}

		/**
		 * Constructor for a matrix of the given order.
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
				matrix = null; // TODO - would we want to allow builder to be re-used? => some sort of reset method
			}
		}
	}
}
