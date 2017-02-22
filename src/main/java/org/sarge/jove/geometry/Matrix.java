package org.sarge.jove.geometry;

import java.nio.FloatBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * 2D floating-point matrix.
 * <p>
 * These matrices are:
 * <ul>
 * <li>square - i.e. same number of rows and columns</li>
 * <li>row major - i.e. elements are accessed by row <b>then</b> by column</li> (however @see {@link #append(FloatBuffer)})
 * <li>immutable - all mutators create a <b>new</b> instance</li>
 * </ul>
 * @author Sarge
 */
public final class Matrix implements Transform, Bufferable {
	/**
	 * 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = identity(4);

	/**
	 * Creates a translation matrix using the given vector.
	 * @param vec Translation vector
	 * @return Translation matrix
	 */
	public static Matrix translation(Vector vec) {
		final MatrixBuilder trans = new MatrixBuilder();
		trans.setColumn(3, vec);
		return trans.build();
	}

	/**
	 * Creates a rotation matrix about the given axis.
	 * @param rot Axis-angle rotation
	 * @return Rotation matrix
	 */
	public static Matrix rotation(Rotation rot) {
		// Calc angles
		final float angle = -rot.getAngle();
		final float sin = MathsUtil.sin(angle);
		final float cos = MathsUtil.cos(angle);

		// Init matrix
		final MatrixBuilder m = new MatrixBuilder(4);

		// Rotate by axis
		final Vector axis = rot.getAxis();
		if(Vector.X_AXIS.equals(axis)) {
			m.set(1, 1, cos);
			m.set(1, 2, sin);
			m.set(2, 1, -sin);
			m.set(2, 2, cos);
		}
		else if(Vector.Y_AXIS.equals(axis)) {
			m.set(0, 0, cos);
			m.set(0, 2, -sin);
			m.set(2, 0, sin);
			m.set(2, 2, cos);
		}
		else if(Vector.Z_AXIS.equals(rot)) {
			m.set(0, 0, cos);
			m.set(0, 1, -sin);
			m.set(1, 0, sin);
			m.set(1, 1, cos);
		}
		else {
			throw new UnsupportedOperationException("Arbitrary rotation axis not implemented");
		}

		return m.build();
	}

	/**
	 * Creates a scaling matrix.
	 * @param x X scale
	 * @param y Y scale
	 * @param z Z scale
	 * @return Scaling matrix
	 */
	public static Matrix scale(float x, float y, float z) {
		final MatrixBuilder scale = new MatrixBuilder(4);
		scale.set(0, 0, x);
		scale.set(1, 1, y);
		scale.set(2, 2, z);
		return scale.build();
	}

	/**
	 * Creates a matrix that is scaled by the given value in all three directions.
	 * @param scale Scalar
	 * @return Scaling matrix
	 */
	public static Matrix scale(float scale) {
		return scale(scale, scale, scale);
	}
	
	/**
	 * Creates an identity matrix.
	 * @param order Matrix order
	 * @return Identity matrix
	 */
	public static Matrix identity(int order) {
		return new MatrixBuilder(order).identity().build();
	}

	private final float[][] matrix;

	/**
	 * Constructor.
	 * @param matrix 2D matrix array
	 */
	public Matrix(float[][] matrix) {
		this(matrix.length);
		for(int n = 0; n < matrix.length; ++n) {
			this.matrix[n] = matrix[n].clone();
		}
	}

	/**
	 * Constructor for an empty matrix.
	 * @param order Matrix order
	 */
	protected Matrix(int order) {
		this.matrix = new float[order][];
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int getOrder() {
		return matrix.length;
	}

	/**
	 * Retrieves the matrix value at the given row and column.
	 * @param r Row
	 * @param c Column
	 * @return Matrix value
	 */
	public float get(int r, int c) {
		return matrix[r][c];
	}

	/**
	 * @return Transpose of this matrix (rows and columns swapped)
	 */
	public Matrix transpose() {
		final Matrix m = new Matrix(matrix.length);
		for(int r = 0; r < matrix.length; ++r) {
			for(int c = 0; c < matrix.length; ++c) {
				m.matrix[r][c] = matrix[c][r];
			}
		}
		return m;
	}

	/**
	 * Extracts a <i>minor</i> from this matrix.
	 * A minor matrix is the n-1 order matrix comprised of this matrix except the i'th row and j'th column.
	 * @param i Row to exclude
	 * @param j column to exclude
	 * @return Minor matrix
	 */
	private Matrix getMinor(int i, int j) {
		final Matrix minor = new Matrix(matrix.length - 1);
		int x = 0;
		for(int r = 0; r < matrix.length; ++r) {
			int y = 0;
			for(int c = 0; c < matrix.length; ++c) {
				minor.matrix[x][y] = this.matrix[r][c];
				if(c != j) ++y;
			}
			if(r != i) ++x;
		}
		return minor;
	}

	/**
	 * @return Determinant of this matrix
	 */
	public float getDeterminant() {
		switch(matrix.length) {
		case 1:
			return matrix[0][0];

		case 2:
			return (matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]);

		default:
			// Sums determinants of the minor matrices (arbitrarily in the first column)
			float total = 0;
			for(int r = 0; r < matrix.length; ++r) {
				float det = matrix[r][0] * getMinor(r, 0).getDeterminant();
				if(!MathsUtil.isEven(r)) det = -det;
				total += det;
			}
			return total;
		}
	}

	/**
	 * @return Cofactor matrix
	 */
	private Matrix cofactor() {
		final Matrix m = new Matrix(matrix.length);
		for(int r = 0; r < matrix.length; ++r) {
			for(int c = 0; c < matrix.length; ++c) {
				float det = getMinor(r, c).getDeterminant();
				if(MathsUtil.isEven(r + c)) det = -det;
				m.matrix[r][c] = det;
			}
		}
		return m;
	}

	/**
	 * Inverts this matrix.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The inverse of a square matrix with a non-zero determinant is the <i>adjoint</i> matrix divided by the determinant.</li>
	 * <li>The adjoint is the transpose of the <i>cofactor</i> matrix.</li>
	 * <li>The cofactor is the matrix of determinants of all minors of this matrix.</li>
	 * </ul>
	 * @return Inverted matrix
	 */
	public Matrix invert() {
		final float det = getDeterminant();
		if(MathsUtil.isZero(det)) throw new IllegalArgumentException("Cannot invert matrix with zero determinant");
		return cofactor().transpose().multiply(1f / det);
	}

	/**
	 * Multiplies this matrix by the given scaling value.
	 * @param scale Scaling value
	 * @return Scaled matrix
	 */
	public Matrix multiply(float scale) {
		final Matrix m = new Matrix(matrix.length);
		for(int r = 0; r < matrix.length; ++r) {
			for(int c = 0; c < matrix.length; ++c) {
				m.matrix[r][c] = this.matrix[r][c] * scale;
			}
		}
		return m;
	}

	/**
	 * Adds a matrix to this matrix.
	 * @param m Matrix to add
	 * @return Summed matrix
	 * @throws IllegalArgumentException if the matrices are not of the same order
	 */
	public Matrix add(Matrix m) {
		if(m.getOrder() != this.getOrder()) throw new IllegalArgumentException("Matrices must have same order");
		final Matrix result = new Matrix(matrix.length);
		for(int r = 0; r < matrix.length; ++r) {
			for(int c = 0; c < matrix.length; ++c) {
				result.matrix[r][c] = this.matrix[r][c] + m.matrix[r][c];
			}
		}
		return result;
	}

	/**
	 * Multiplies this matrix with another matrix.
	 * @param m Matrix
	 * @return Multiplied matrix
	 * @throws IllegalArgumentException if the matrices are not of the same order
	 */
	public Matrix multiply(Matrix m) {
		final Matrix result = new Matrix(matrix.length);
		multiply(this.matrix, m, result);
		return result;
	}

	/**
	 * Multiplies the given matrix with another matrix and stores the result.
	 * @param matrix	This matrix
	 * @param that		Matrix to multiply
	 * @param result	Result matrix
	 */
	protected static void multiply(float[][] matrix, Matrix that, Matrix result) {
		if(matrix.length != that.getOrder()) throw new IllegalArgumentException("Matrices must have same order");
		float total;
		for(int r = 0; r < matrix.length; ++r) {
			for(int c = 0; c < matrix.length; ++c) {
				total = 0;
				for(int n = 0; n < matrix.length; ++n) {
					total += matrix[r][n] * that.matrix[n][c];
				}
				result.matrix[r][c] = total;
			}
		}
	}

	/**
	 * Multiplies the given point by this matrix.
	 * @param pos Point to multiply
	 * @return Multiplied point
	 * @throws IllegalArgumentException if this matrix is too small
	 * TODO - investigate difference between this and vec4 with 0 or 1
	 */
	public Point multiply(Point pos) {
		if(getOrder() < 3) throw new IllegalArgumentException("Matrix too small");
		return new Point(
			multiply(0, pos),
			multiply(1, pos),
			multiply(2, pos)
		);
	}

	private float multiply(int r, Point pt) {
		return matrix[r][0] * pt.x + matrix[r][1] * pt.y + matrix[r][2] * pt.z;
	}

	/**
	 * Extracts a portion of this matrix (top-left).
	 * @param size Sub-matrix size
	 * @return Sub-matrix
	 */
	public Matrix getSubMatrix(int size) {
		if(size >= getOrder()) throw new IllegalArgumentException("Sub-matrix too large");
		final Matrix m = new Matrix(size);
		for(int r = 0; r < size; ++r) {
			for(int c = 0; c < size; ++c) {
				m.matrix[r][c] = this.matrix[r][c];
			}
		}
		return m;
	}

	/**
	 * @return Position of this matrix
	 */
	public Point getPosition() {
		return new Point(
			matrix[0][3],
			matrix[1][3],
			matrix[2][3]
		);
	}

	@Override
	public int getComponentSize() {
		return getOrder();
	}

	/**
	 * Adds this matrix to the given NIO buffer in OpenGL <b>column-major</b> order.
	 * @param buffer
	 */
	@Override
	public void append(FloatBuffer buffer) {
		for(int c = 0; c < matrix.length; ++c) {
			for(int r = 0; r < matrix.length; ++r) {
				buffer.put(matrix[r][c]);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof Matrix) {
			final Matrix m = (Matrix) obj;
			if(m.getOrder() != this.getOrder()) return false;
			for(int r = 0; r < matrix.length; ++r) {
				for(int c = 0; c < matrix.length; ++c) {
					if(!MathsUtil.isEqual(this.matrix[r][c], m.matrix[r][c])) return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Matrix toMatrix() {
		return this;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for(int r = 0; r < matrix.length; ++r) {
			if(r > 0) sb.append('\n');
			for(int c = 0; c < matrix.length; ++c) {
				if(c > 0) sb.append(',');
				sb.append(String.format("%10.5f", matrix[r][c]));
			}
		}
		return sb.toString();
	}
}
