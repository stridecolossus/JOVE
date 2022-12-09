package org.sarge.jove.geometry;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
 * <li>Matrix data written by {@link #buffer(ByteBuffer)} is <i>column major</i> as expected by Vulkan</li>
 * </ul>
 * <p>
 * @see Matrix4
 * @author Sarge
 */
public class Matrix implements Transform, Bufferable {
	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param order Matrix order
	 */
	protected Matrix(int order) {
		Check.oneOrMore(order);
		this.matrix = new float[order][order];
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
	 * @param row Row
	 * @param col Column
	 * @return Matrix element
	 * @throws IndexOutOfBoundsException if {@link #row} or {@link #col} are out-of-bounds
	 */
	public float get(int row, int col) {
		return matrix[row][col];
	}

	/**
	 * Extracts a matrix row as a vector.
	 * @param row Row index
	 * @return Matrix row
	 * @throws IndexOutOfBoundsException if the row index is invalid or the matrix is too small
	 */
	public final Vector row(int row) throws IndexOutOfBoundsException {
		final float x = matrix[row][0];
		final float y = matrix[row][1];
		final float z = matrix[row][2];
		return new Vector(x, y, z);
	}

	/**
	 * Extracts a matrix column as a vector.
	 * @param col Column index
	 * @return Matrix column
	 * @throws IndexOutOfBoundsException if the column index is invalid or the matrix is too small
	 */
	public final Vector column(int col) throws IndexOutOfBoundsException {
		final float x = matrix[0][col];
		final float y = matrix[1][col];
		final float z = matrix[2][col];
		return new Vector(x, y, z);
	}

	/**
	 * Extracts a submatrix from this matrix.
	 * @param row 		Row offset
	 * @param col 		Column offset
	 * @param order		Submatrix order
	 * @return Submatrix
	 * @throws IndexOutOfBoundsException if the submatrix is out-of-bounds for this matrix
	 */
	public final Matrix submatrix(int row, int col, int order) throws IndexOutOfBoundsException {
		final var sub = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				final float f = this.matrix[r + row][c + col];
				sub.matrix[r][c] = f;
			}
		}
		return sub;
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = this.order();
		final Matrix transpose = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				transpose.matrix[r][c] = this.matrix[c][r];
			}
		}
		return transpose;
	}

	/**
	 * Multiplies this and the given matrix.
	 * <p>
	 * The resultant matrix first applies the given matrix and <b>then</b> this matrix, i.e. <code>A * B</code> applies B then A.
	 * <p>
	 * Note that matrix multiplication is <b>non-commutative</b>.
	 * <p>
	 * @param m Matrix
	 * @return Multiplied matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	public final Matrix multiply(Matrix m) {
		// Check same sized matrices
		final int order = order();
		if(m.order() != order) throw new IllegalArgumentException("Cannot multiply matrices with different orders");

		// Multiply matrices
		final Matrix result = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total = Math.fma(this.matrix[r][n], m.matrix[n][c], total);
				}
				result.matrix[r][c] = total;
			}
		}

		return result;
	}

	@Override
	public final void buffer(ByteBuffer buffer) {
		final int order = order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				buffer.putFloat(matrix[c][r]);
			}
		}
	}

	@Override
	public final boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Matrix that) &&
				isEqual(that);
	}

	private boolean isEqual(Matrix that) {
		final int order = this.order();
		if(order != that.order()) {
			return false;
		}
		for(int r = 0; r < order; ++r) {
			if(!MathsUtil.isEqual(this.matrix[r], that.matrix[r])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return String representation of this matrix
	 */
	public String dump() {
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

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("order", order()).build();
	}

	/**
	 * Builder for a matrix.
	 */
	public static class Builder {
		private Matrix matrix;

		/**
		 * Constructor for a matrix of the given order.
		 * @param order Matrix order
		 * @throws IllegalArgumentException for an illogical matrix order
		 */
		public Builder(int order) {
			this.matrix = new Matrix(order);
		}

		/**
		 * Initialises to the identity matrix.
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
		 * @throws IndexOutOfBoundsException if {@link #row} or {@link #col} is out-of-bounds
		 */
		public Builder set(int row, int col, float value) {
			matrix.matrix[row][col] = value;
			return this;
		}

		/**
		 * Sets a matrix row to the given vector.
		 * @param row		Row index
		 * @param vec 		Vector
		 * @throws IndexOutOfBoundsException if {@link #row} is out-of-bounds
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
		 * @throws IndexOutOfBoundsException if {@link #col} is out-of-bounds
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
