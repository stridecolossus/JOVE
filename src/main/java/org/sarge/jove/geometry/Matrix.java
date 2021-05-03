package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>matrix</i> is a 2-dimensional square array used for geometry transformation and projection.
 * <p>
 * Matrix properties:
 * <ul>
 * <li>A matrix has an <i>order</i> that specifies the dimensions of the matrix</li>
 * <li>Matrix data is assumed to be <i>column major</i> (this is the Vulkan default)</li>
 * <li>Matrices are {@link Bufferable}</li>
 * </ul>
 * <p>
 * Matrices can be created via public constructors but in general a matrix is constructed using a builder:
 * <p>
 * <pre>
 * Matrix matrix = new Matrix.Builder()
 * 	.identity()
 * 	.set(row, col, value)
 * 	.build();
 * </pre>
 * <p>
 * @author Sarge
 */
public interface Matrix extends Transform, Bufferable {
	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	static Matrix identity(int order) {
		return new Builder(order).identity().build();
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	int order();

	/**
	 * @return Matrix as a one-dimensional column-major array
	 */
	float[] array();

	@Override
	default Matrix matrix() {
		return this;
	}

	/**
	 * Helper - Calculates the column-major matrix index for the given row and column.
	 * @param row			Row
	 * @param col			Column
	 * @param order			Matrix order
	 * @return Matrix index
	 * @throws IllegalArgumentException for an invalid row or column index
	 */
	static int index(int row, int col, int order) {
		if((row >= order) || (col >= order)) throw new IllegalArgumentException(String.format("Invalid row/column: row=%d col=%d order=%d", row, col, order));
		return row + col * order;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 * @throws ArrayIndexOutOfBoundsException if the row or column are out-of-bounds
	 */
	float get(int row, int col);

	/**
	 * Extracts a matrix row as a vector.
	 * @param row Row index
	 * @return Matrix row
	 * @throws ArrayIndexOutOfBoundsException if the row index is invalid or the matrix is too small
	 */
	Vector row(int row);

	/**
	 * Extracts a matrix column as a vector.
	 * @param col Column index
	 * @return Matrix column
	 * @throws ArrayIndexOutOfBoundsException if the column index is invalid or the matrix is too small
	 */
	Vector column(int col);

	/**
	 * @return Transpose of this matrix
	 */
	Matrix transpose();

	/**
	 * Multiplies this and the given matrix.
	 * @param m Matrix
	 * @return New matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	Matrix multiply(Matrix m);

	// TODO
	// - determinant, invert, etc
	// - SIMD/vector operations?

	/**
	 * Builder for a matrix.
	 */
	class Builder {
		private final int order;
		private final float[] matrix;

		/**
		 * Constructor for a matrix of the given order.
		 * @param order Matrix order
		 */
		public Builder(int order) {
			this.order = oneOrMore(order);
			this.matrix = new float[order * order];
		}

		/**
		 * Initialises this matrix to identity (only affects the diagonal elements).
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
			if(order == Matrix4.SIZE) {
				return new Matrix4(matrix);
			}
			else {
				return new DefaultMatrix(matrix);
			}
		}
	}
}
