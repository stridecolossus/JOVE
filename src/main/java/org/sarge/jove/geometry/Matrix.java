package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

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
public class Matrix implements Transform, Bufferable {
	private static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	public static Matrix identity(int order) {
		return new Builder(order).identity().build();
	}

	private final float[] matrix;

	/**
	 * Constructor.
	 * @param matrix Matrix array in column-major order
	 */
	protected Matrix(float[] matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int order() {
		return switch(matrix.length) {
			case 1 -> 1;
			case 4 -> 2;
			case 9 -> 3;
			case 16 -> 4;
			default -> (int) MathsUtil.sqrt(matrix.length);
		};
	}

	@Override
	public final Matrix matrix() {
		return this;
	}

	/**
	 * Helper - Calculates the column-major matrix index for the given row and column.
	 * @param row			Row
	 * @param col			Column
	 * @param order			Matrix order
	 * @return Matrix index
	 */
	protected static int index(int row, int col, int order) {
		assert validate(row, order);
		assert validate(col, order);
		return row + col * order;
	}

	private static boolean validate(int index, int order) {
		return (index >= 0) && (index < order);
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 * @throws ArrayIndexOutOfBoundsException if the row or column are out-of-bounds
	 */
	public float get(int row, int col) {
		final int index = index(row, col, order());
		return matrix[index];
	}

	/**
	 * @return Matrix as a 1D column-major array
	 */
	public float[] array() {
		return Arrays.copyOf(matrix, matrix.length);
	}

	/**
	 * Extracts a matrix row as a vector.
	 * @param row Row index
	 * @return Matrix row
	 * @throws ArrayIndexOutOfBoundsException if the row index is invalid or the matrix is too small
	 */
	public Vector row(int row) {
		final int order = order();
		final int index = index(row, 0, order);
		final float x = matrix[index];
		final float y = matrix[index + order];
		final float z = matrix[index + 2 * order];
		return new Vector(x, y, z);
	}

	/**
	 * Extracts a matrix column as a vector.
	 * @param col Column index
	 * @return Matrix column
	 * @throws ArrayIndexOutOfBoundsException if the column index is invalid or the matrix is too small
	 */
	public Vector column(int col) {
		final int index = Matrix.index(0, col, order());
		final float x = matrix[index];
		final float y = matrix[index + 1];
		final float z = matrix[index + 2];
		return new Vector(x, y, z);
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(float f : matrix) {
			buffer.putFloat(f);
		}
	}

	@Override
	public int length() {
		return matrix.length * Float.BYTES;
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = order();
		final Builder trans = new Builder(order);
		int dest = 0;
		for(int r = 0; r < order; ++r) {
			int src = r;
			for(int c = 0; c < order; ++c) {
				trans.matrix[dest] = matrix[src];
				++dest;
				src += order;
			}
		}
		return trans.build();
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
		final Builder result = new Builder(order);
// TODO - avoid get() and calc indices once and increment, otherwise this will be slow
		int dest = 0;
		for(int c = 0; c < order; ++c) {
			for(int r = 0; r < order; ++r) {
				// Sum this row by the corresponding column
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total += get(r, n) * m.get(n, c);
				}

				// Set result element
				result.matrix[dest] = total;
				++dest;
			}
		}
		return result.build();
	}

	// TODO
	// - determinant, invert, etc
	// - SIMD/vector operations?

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Matrix that) &&
				(this.order() == that.order()) &&
				MathsUtil.isEqual(this.matrix, that.matrix);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int order = this.order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				sb.append(String.format("%10.5f ", get(r, c)));
			}
			sb.append(LINE_SEPARATOR);
		}
		return sb.toString();
	}

	/**
	 * Builder for a matrix.
	 */
	public static class Builder {
		private final int order;
		private final float[] matrix;

		/**
		 * Constructor for a matrix builder of the given order.
		 * @param order Matrix order
		 * @throws IllegalArgumentException for an illogical matrix order
		 */
		public Builder(int order) {
			this.order = oneOrMore(order);
			this.matrix = new float[order * order];
		}

		/**
		 * Initialises this matrix to identity.
		 */
		public Builder identity() {
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
			final int index = index(row, col, order);
			matrix[index] = value;
			return this;
		}

		// TODO - bulk setter to create matrix from array?

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
			if(order == Matrix4.ORDER) {
				return new Matrix4(matrix);
			}
			else {
				return new Matrix(matrix);
			}
		}
	}
}
