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
 * @author Sarge
 */
public abstract class Matrix implements Transform, Bufferable {
	/**
	 * Creates a new matrix of the given order.
	 * @param order Matrix order
	 * @return New matrix
	 */
	private static Matrix create(int order) {
		if(order == Matrix4.ORDER) {
			return new Matrix4();
		}
		else {
			return new Matrix(order) {
				@Override
				public int order() {
					return order;
				}
			};
		}
	}

	private final float[] matrix;

	/**
	 * Constructor.
	 * @param order Matrix order
	 */
	protected Matrix(int order) {
		Check.oneOrMore(order);
		this.matrix = new float[order * order];
	}

	/**
	 * @return Order (or size) of this matrix
	 */
	public abstract int order();

	@Override
	public final Matrix matrix() {
		return this;
	}

	/**
	 * Retrieves a matrix element.
	 * @param row Row
	 * @param col Column
	 * @return Matrix element
	 * @throws IndexOutOfBoundsException if {@link #row} or {@link #col} are out-of-bounds
	 */
	public float get(int row, int col) throws IndexOutOfBoundsException {
		check(row);
		check(col);
		return getLocal(row, col);
	}

	private void check(int index) {
		if((index < 0) || (index >= order())) throw new IndexOutOfBoundsException(index);
	}

	private float getLocal(int row, int col) {
		final int index = index(row, col);
		return matrix[index];
	}

	/**
	 * @return Matrix index for the given matrix element
	 */
	protected int index(int row, int col) {
		return row + col * order();
	}

	/**
	 * Extracts a matrix row as a vector.
	 * @param row Row index
	 * @return Matrix row
	 * @throws IndexOutOfBoundsException if the row index is invalid or the matrix is too small
	 */
	public Vector row(int row) throws IndexOutOfBoundsException {
		check(row);
		final int order = order();
		final float x = matrix[row];
		final float y = matrix[row + order];
		final float z = matrix[row + 2 * order];
		return new Vector(x, y, z);
	}

	/**
	 * Extracts a matrix column as a vector.
	 * @param col Column index
	 * @return Matrix column
	 * @throws IndexOutOfBoundsException if the column index is invalid or the matrix is too small
	 */
	public Vector column(int col) throws IndexOutOfBoundsException {
		check(col);
		final int order = order();
		final int index = col * order;
		final float x = matrix[index];
		final float y = matrix[index + 1];
		final float z = matrix[index + 2];
		return new Vector(x, y, z);
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		if(buffer.isDirect()) {
			for(float f : matrix) {
				buffer.putFloat(f);
			}
		}
		else {
			buffer.asFloatBuffer().put(matrix);
		}
	}

	@Override
	public int length() {
		return matrix.length * Float.BYTES;
	}

// import jdk.incubator.vector.*;
// https://jbaker.io/2022/06/09/vectors-in-java/
//
//	private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
//
//	/**
//	 * @return Transpose of this matrix
//	 */
//	public Matrix transpose() {
//		// TODO - cache by order? or factory and override for Matrix4?
//		final int order = this.order();
//		final Matrix transpose = create(order);
//
//		final int[] index = transpose(order);
//
//		final int bound = SPECIES.loopBound(matrix.length);
//		int n = 0;
//		for(; n < bound; n += SPECIES.length()) {
//			final VectorShuffle<Float> shuffle = VectorShuffle.fromArray(SPECIES, index, n);
//			final FloatVector vec = FloatVector.fromArray(SPECIES, this.matrix, n);
//			vec.rearrange(shuffle).intoArray(transpose.matrix, n);
//		}
//
//		for(; n < matrix.length; ++n) {
//			transpose.matrix[n] = this.matrix[index[n]];
//		}
//
//		return transpose;
//	}

	/**
	 * @return Transpose of this matrix
	 */
	public Matrix transpose() {
		final int order = this.order();
		final int[] index = transpose(order);
		final Matrix transpose = create(order);
		for(int n = 0; n < matrix.length; ++n) {
			transpose.matrix[n] = this.matrix[index[n]];
		}
		return transpose;
	}

	/**
	 * Builds the transpose index for this matrix.
	 */
	private int[] transpose(int order) {
		final int[] transpose = new int[order * order];
		int index = 0;
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				transpose[index++] = index(r, c);
			}
		}
		return transpose;
	}

	/**
	 * Multiplies two matrices together.
	 * <p>
	 * The resultant matrix first applies the given matrix and <b>then</b> this matrix, i.e. <code>A * B</code> applies B then A.
	 * <p>
	 * Note that matrix multiplication is <b>non-commutative</b>.
	 * <p>
	 * @param m Matrix
	 * @return New matrix
	 * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
	 */
	public Matrix multiply(Matrix m) {
		// Check same sized matrices
		final int order = order();
		if(m.order() != order) throw new IllegalArgumentException("Cannot multiply matrices with different orders");

		// Multiply matrices
		final Matrix result = create(order);
		int index = 0;
		for(int c = 0; c < order; ++c) {
			for(int r = 0; r < order; ++r) {
				float total = 0;
				for(int n = 0; n < order; ++n) {
					total = Math.fma(this.getLocal(r, n), m.getLocal(n, c), total);
				}
				result.matrix[index++] = total;
			}
		}

		return result;
	}
	// TODO - works but could be much more efficient when calculating array indices?
	// TODO - JDK19 vector API

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Matrix that) &&
				(this.order() == that.order()) &&
				MathsUtil.isEqual(this.matrix, that.matrix);
	}

	/**
	 * @return String representation of this matrix
	 */
	public String dump() {
		final StringBuilder sb = new StringBuilder();
		final int order = this.order();
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				sb.append(String.format("%10.5f ", getLocal(r, c)));
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
		 * Default constructor for a {@link Matrix4}.
		 */
		public Builder() {
			this(Matrix4.ORDER);
		}

		/**
		 * Constructor for a matrix of the given order.
		 * @param order Matrix order
		 * @throws IllegalArgumentException for an illogical matrix order
		 */
		public Builder(int order) {
			this.matrix = create(order);
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
			matrix.check(row);
			matrix.check(col);
			final int index = matrix.index(row, col);
			matrix.matrix[index] = value;
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
