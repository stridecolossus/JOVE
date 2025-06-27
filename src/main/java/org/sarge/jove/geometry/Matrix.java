package org.sarge.jove.geometry;

import static org.sarge.lib.Validation.requireOneOrMore;

import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.*;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtility;

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
 * @author Sarge
 */
public class Matrix implements Transform, Bufferable {
	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	public static Matrix identity(int order) {
		return new Builder(order).identity().build();
	}

	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param order Matrix order
	 */
	protected Matrix(int order) {
		requireOneOrMore(order);
		this.matrix = new float[order][order];
	}

	/**
	 * Constructor.
	 * @param matrix Matrix as a 2D array
	 * @throws IllegalArgumentException if the matrix is not square
	 */
	public Matrix(float[][] matrix) {
		final int order = matrix.length;
		// TODO - this used to work...
//		this(order);
		requireOneOrMore(order);
		this.matrix = new float[order][order];
		// ...TODO
		for(int r = 0; r < order; ++r) {
			if(matrix[r].length != order) {
				throw new IllegalArgumentException();
			}
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

	/**
	 * @throws IllegalArgumentException if the given order does not match this matrix
	 */
	protected int validate(int order) {
		if(order != this.order()) {
			throw new IllegalArgumentException(String.format("Invalid matrix order: expected=%d actual=%d", this.order(), order));
		}
		return order;
	}

	@Override
	public final Matrix matrix() {
		return this;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row 		Row index
	 * @param col		Column index
	 * @return Matrix element
	 * @throws IndexOutOfBoundsException if {@link #row} or {@link #col} are out-of-bounds
	 */
	public float get(int row, int col) {
		return matrix[row][col];
	}

	/**
	 * Extracts a submatrix from this matrix.
	 * @param row 		Row offset
	 * @param col 		Column offset
	 * @param order		Submatrix order
	 * @return Submatrix
	 * @throws IndexOutOfBoundsException if the submatrix is out-of-bounds for this matrix
	 */
	public Matrix submatrix(int row, int col, int order) throws IndexOutOfBoundsException {
		final var sub = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				sub.matrix[r][c] = this.matrix[r + row][c + col];
			}
		}
		return sub;
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = this.order();
		final var transpose = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				transpose.matrix[r][c] = this.matrix[c][r];
			}
		}
		return transpose;
	}

	/**
	 * Sums this and the given matrix.
	 * @param that Matrix to add
	 * @return Summed matrix
	 * @throws IllegalArgumentException if the given order does not match this matrix
	 */
	public Matrix add(Matrix that) {
		final int order = this.order();
		final Matrix result = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				result.matrix[r][c] = this.matrix[r][c] + that.matrix[r][c];
			}
		}
		return result;
	}

	/**
	 * Multiplies this matrix by the given scalar.
	 * @param scalar Multiplier scalar
	 * @return Scaled matrix
	 * @throws IllegalArgumentException if the given order does not match this matrix
	 */
	public Matrix multiply(float scalar) {
		final int order = this.order();
		final Matrix result = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				result.matrix[r][c] = this.matrix[r][c] * scalar;
			}
		}
		return result;
	}

	/**
	 * Multiplies this and the given matrix.
	 * <p>
	 * The resultant matrix first applies the given matrix and <b>then</b> this matrix, i.e. <code>A * B</code> applies B then A.
	 * Note that matrix multiplication is <b>non-commutative</b>.
	 * <p>
	 * @param that Matrix to multiply
	 * @return Multiplied matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	public Matrix multiply(Matrix that) {
		final int order = validate(that.order());
		final var result = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total = Math.fma(this.matrix[r][n], that.matrix[n][c], total);
				}
				result.matrix[r][c] = total;
			}
		}
		return result;
	}

	/**
	 * Transforms the given vector by this matrix.
	 * @param vector Vector to transform
	 * @return Transformed vector
	 * @throws IllegalArgumentException if the vector is not the same length as the order of this matrix
	 */
	public float[] multiply(float[] vector) {
		final int order = validate(vector.length);
		final float[] result = new float[order];
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				result[r] += matrix[r][c] * vector[c];
			}
		}
		return result;
	}

	/**
	 * Computes the <i>outer product</i> of the given vectors as a matrix.
	 * @return Outer product
	 * @throws IllegalArgumentException if the arrays are not of the same length
	 * @see <a href="https://en.wikipedia.org/wiki/Outer_product">Wikipedia</a>
	 */
	public static Matrix outer(float[] left, float[] right) {
		if(left.length != right.length) {
			throw new IllegalArgumentException("Cannot multiply vectors of different lengths");
		}
		final int order = left.length;
		final Matrix matrix = new Matrix(order);
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				matrix.matrix[r][c] = left[r] * right[c];
			}
		}
		return matrix;
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
	public int hashCode() {
		return Arrays.deepHashCode(matrix);
	}

	@Override
	public boolean equals(Object obj) {
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
			for(int c = 0; c < order; ++c) {
    			if(!MathsUtility.isApproxEqual(this.matrix[r][c], that.matrix[r][c])) {
    				return false;
    			}
			}
		}
		return true;
	}

	/**
	 * Builds a multi-line string representation of this matrix.
	 * @param format Formatter
	 * @return Matrix string
	 */
	public String format(NumberFormat format) {
		final var str = new StringJoiner("\n");
		for(int r = 0; r < this.order(); ++r) {
			final String row = MathsUtility.format(matrix[r]);
			str.add(row);
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return format(MathsUtility.FORMATTER);
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
			for(int n = 0; n < matrix.order(); ++n) {
				set(n, n, 1);
			}
			return this;
		}

		/**
		 * Sets a matrix element.
		 * @param row 		Row index
		 * @param col		Column index
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
		 * @param vector 		Vector
		 * @throws IndexOutOfBoundsException if {@link #row} is out-of-bounds
		 */
		public Builder row(int row, Vector vector) {
			set(row, 0, vector.x);
			set(row, 1, vector.y);
			set(row, 2, vector.z);
			return this;
		}

		/**
		 * Sets a matrix column to the given vector.
		 * @param col 			Column index
		 * @param vector		Vector
		 * @throws IndexOutOfBoundsException if {@link #col} is out-of-bounds
		 */
		public Builder column(int col, Vector vector) {
			set(0, col, vector.x);
			set(1, col, vector.y);
			set(2, col, vector.z);
			return this;
		}

		/**
		 * Populates a submatrix of this matrix.
		 * @param row			Row index
		 * @param col			Column index
		 * @param submatrix		Submatrix
		 * @throws IndexOutOfBoundsException if the submatrix is out-of-bounds
		 */
		public Builder submatrix(int row, int col, Matrix submatrix) {
			final int order = submatrix.order();
			for(int r = 0; r < order; ++r) {
				for(int c = 0; c < order; ++c) {
					matrix.matrix[r + row][c + col] = submatrix.matrix[r][c];
				}
			}
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
