package org.sarge.jove.geometry;

/**
 * Builder for a {@link Matrix}.
 * @author Sarge
 */
public class MatrixBuilder {
	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param order Matrix order
	 */
	public MatrixBuilder(int order) {
		matrix = new float[order][order];
	}

	/**
	 * Default constructor for a 4x4 matrix.
	 */
	public MatrixBuilder() {
		this(4);
	}
	
	/**
	 * Initialises the matrix to identity.
	 * @return This builder
	 */
	public MatrixBuilder identity() {
		for(int n = 0; n < matrix[0].length; ++n) {
			matrix[n][n] = 1;
		}
		return this;
	}

	/**
	 * Sets the specified matrix element.
	 * @param r Row
	 * @param c Column
	 * @param f Value to set
	 * @return This builder
	 */
	public MatrixBuilder set(int r, int c, float f) {
		matrix[r][c] = f;
		return this;
	}

	/**
	 * Sets the first 3 values of a row to the given tuple.
	 * @param row	Row to set
	 * @param vec	Vector
	 * @return This builder
	 */
	public MatrixBuilder setRow(int row, Tuple vec) {
		matrix[row][0] = vec.x;
		matrix[row][1] = vec.y;
		matrix[row][2] = vec.z;
		return this;
	}

	/**
	 * Sets the top 3 values of a column to the given tuple.
	 * @param col	Column to set
	 * @param vec	Vector
	 * @return This builder
	 */
	public MatrixBuilder setColumn(int col, Tuple vec) {
		matrix[0][col] = vec.x;
		matrix[1][col] = vec.y;
		matrix[2][col] = vec.z;
		return this;
	}

	/**
	 * Constructs a matrix.
	 * @return New matrix
	 */
	public Matrix build() {
		return new Matrix(matrix);
	}
	
	@Override
	public String toString() {
		return new Matrix(matrix).toString();
	}
}
