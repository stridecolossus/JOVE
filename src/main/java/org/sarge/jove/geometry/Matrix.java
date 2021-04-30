package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>matrix</i> is a 2-dimensional square array used for geometry transformation and projection.
 * <p>
 * Notes:
 * <ul>
 * <li>the matrix is stored <i>column major</i> for buffering convenience</li>
 * </ul>
 * @author Sarge
 */
// TODO
// - determinant, invert, etc
// - SIMD/vector operations?
public final class Matrix implements Transform, Bufferable {

	// http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
	// https://stackoverflow.com/questions/28075743/how-do-i-compose-a-rotation-matrix-with-human-readable-angles-from-scratch/28084380#28084380
	//https://www.reddit.com/r/vulkan/comments/9l7s2y/vulkan_fps_camera_example/

	private static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Default order size for a matrix.
	 */
	private static final int DEFAULT_ORDER = 4;

	/**
	 * Convenience 4x4 identity matrix.
	 */
	public static final Matrix IDENTITY = new Matrix.Builder(DEFAULT_ORDER).identity().build();

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
	 * Creates a scaling matrix.
	 * @param scale Scaling tuple (for all three directions)
	 * @return Scaling matrix
	 */
	public static Matrix scale(float scale) {
		return scale(scale, scale, scale);
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
			// TODO - return Quaternion(rotation)?
		}
		return rot.build();
	}

	private final int order;
	private final float[] matrix;

	/**
	 * Constructor.
	 * @param matrix Column-major matrix elements
	 * @throws IllegalArgumentException if the matrix is not square or the array length does not match the matrix order
	 */
	public Matrix(float[] matrix) {
		this(order(matrix.length), Arrays.copyOf(matrix, matrix.length));
	}

	/**
	 * Copy constructor.
	 * @param order			Matrix order
	 * @param matrix		Matrix
	 */
	private Matrix(int order, float[] matrix) {
		assert matrix.length == order * order;
		this.order = order;
		this.matrix = matrix;
	}

	/**
	 * Determines the order of the matrix.
	 * @param len Matrix length
	 * @return Order
	 * @throws IllegalArgumentException if the given length is not square
	 */
	private static int order(int len) {
		return switch(len) {
			case 1 -> 1;
			case 4 -> 2;
			case 9 -> 3;
			case 16 -> 4;
			default -> {
				final int order = (int) MathsUtil.sqrt(len);
				if(len != order * order) throw new IllegalArgumentException("Matrix must be square");
				yield order;
			}
		};
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public int order() {
		return order;
	}

	@Override
	public Matrix matrix() {
		return this;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(float f : matrix) {
			buffer.putFloat(f);
		}
	}

	@Override
	public int length() {
		return order * order * Float.BYTES;
	}

	/**
	 * Determines the column-major index into the array.
	 */
	private static int index(int order, int row, int col) {
		return row + order * col;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 * @throws ArrayIndexOutOfBoundsException if the row or column is out-of-bounds
	 */
	public float get(int row, int col) {
		final int index = index(order, row, col);
		return matrix[index];
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final float[] trans = new float[matrix.length];
		int index = 0;
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				trans[index] = get(r, c);
				++index;
			}
		}
		return new Matrix(order, trans);
	}

	/**
	 * Multiplies two matrices.
	 * @param m Matrix
	 * @return New matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	public Matrix multiply(Matrix m) {
		if(m.order != order) throw new IllegalArgumentException("Cannot multiply matrices with different sizes");

		// Multiply matrices
		final float[] result = new float[matrix.length];
		int index = 0;
		for(int c = 0; c < order; ++c) {
			for(int r = 0; r < order; ++r) {
				// Sum this row by the corresponding column
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total += get(r, n) * m.get(n, c);
				}

				// Set result element
				result[index] = total;
				++index;
			}
		}

		// Create resultant matrix
		return new Matrix(order, result);
	}

	// TODO
	public Point multiply(Point pt) {
		// Convert to homogeneous array
		if(order != DEFAULT_ORDER) throw new IllegalArgumentException("Can only multiply a vector by a matrix with order of 4");
		final float[] array = {pt.x, pt.y, pt.z, 1};
//		final float[] array = new float[4];
//		array[0] = pt.x;
//		array[1] = pt.y;
//		array[2] = pt.z;
//		array[3] = 1;

		// Multiply
		final float[] result = new float[4];
		for(int r = 0; r < order; ++r) {
			float total = 0;
			for(int c = 0; c < order; ++c) {
				total += get(r, c) * array[c];
			}
			result[r] = total;
		}

		// Convert back to point
		return new Point(result[0], result[1], result[2]);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof Matrix that) &&
				(this.order == that.order) &&
				MathsUtil.isEqual(this.matrix, that.matrix);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				if(c > 0) {
					sb.append(",");
				}
				sb.append(String.format("%10.5f", get(r, c)));
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
		private float[] matrix;

		/**
		 * Constructor for a matrix of the given order.
		 * @param order Matrix order
		 */
		public Builder(int order) {
			this.order = oneOrMore(order);
			this.matrix = new float[order * order];
		}

		/**
		 * Constructor for a matrix with an order {@link Matrix#DEFAULT_ORDER}.
		 */
		public Builder() {
			this(DEFAULT_ORDER);
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
			final int index = index(order, row, col);
			matrix[index] = value;
			return this;
		}

		/**
		 * Sets a matrix row to the given vector.
		 * @param row		Row index
		 * @param vec 		Vector
		 * @throws ArrayIndexOutOfBoundsException if the row is out-of-bounds
		 */
		public Builder row(int row, Vector vec) {
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
		public Builder column(int col, Vector vec) {
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
			final Matrix result = new Matrix(order, matrix);
			this.matrix = null;
			return result;
		}
	}
}
