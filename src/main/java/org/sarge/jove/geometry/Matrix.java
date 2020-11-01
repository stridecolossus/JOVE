package org.sarge.jove.geometry;

import static org.sarge.jove.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>matrix</i> is a 2D square array used for geometry transformation and projection.
 * <p>
 * Notes:
 * <ul>
 * <li>the matrix is stored <i>column major</i> for buffering convenience</li>
 * </ul>
 * @author Sarge
 */
// TODO
// - determinant, invert, etc
public final class Matrix implements Transform, Bufferable {

	// http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
	// https://stackoverflow.com/questions/28075743/how-do-i-compose-a-rotation-matrix-with-human-readable-angles-from-scratch/28084380#28084380
	//https://www.reddit.com/r/vulkan/comments/9l7s2y/vulkan_fps_camera_example/

	private static final String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * Default order size for a matrix.
	 */
	public static final int DEFAULT_ORDER = 4;

	/**
	 * Default identity matrix.
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
			// TODO - return Quaternion(rotation)?
		}
		return rot.build();
	}

	private final int order;
	private final float[] matrix;

	/**
	 * Constructor.
	 * @param order		Matrix order
	 * @param matrix 	Column-major matrix elements
	 * @throws IllegalArgumentException if the length of the given array does not match the specified matrix order
	 */
	public Matrix(int order, float[] matrix) {
		if(matrix.length != order * order) throw new IllegalArgumentException("Invalid matrix length");
		this.order = oneOrMore(order);
		this.matrix = Arrays.copyOf(matrix, matrix.length);
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
	public long length() {
		return order * order * Float.BYTES;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Matrix row
	 * @param col Column
	 * @return Matrix element
	 * @throws ArrayIndexOutOfBoundsException if the row or column is out-of-bounds
	 */
	public float get(int row, int col) {
		return matrix[index(row, col)];
	}

	/**
	 * @param row
	 * @param col
	 * @return Matrix index
	 */
	private int index(int row, int col) {
		return row + order * col;
	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final float[] trans = new float[matrix.length];
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				final int index = index(c, r);			// Note row-column interchanged
				trans[index] = get(r, c);
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

		final float[] result = new float[matrix.length];
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total += get(r, n) * m.get(n, c);
				}
				final int index = index(r, c);
				result[index] = total;
			}
		}

		return new Matrix(order, result);
	}

	public Point multiply(Point pt) {
		// Convert to homogeneous array
		if(order != DEFAULT_ORDER) throw new IllegalArgumentException("Can only multiply a vector by a matrix with order of 4");
		final float[] array = new float[4];
		array[0] = pt.x;
		array[1] = pt.y;
		array[2] = pt.z;
		array[3] = 1;

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
		if(obj == this) {
			return true;
		}

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
		private final float[] matrix;
		private final int order;

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
		 * Initialises this matrix to the identity matrix.
		 * Invoking this method on a builder that has already been mutated is undefined, i.e. this method should be invoked <b>first</b> if required.
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
			final int index = row + order * col;
			matrix[index] = value;
			return this;
		}

		/**
		 * Sets a matrix row to the given vector.
		 * @param row Row index
		 * @param vec Vector
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
		 * @param col Column index
		 * @param vec Vector
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
			return new Matrix(order, matrix);
		}
	}
}
