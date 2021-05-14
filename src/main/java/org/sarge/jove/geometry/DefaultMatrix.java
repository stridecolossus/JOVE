package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>default matrix</i> is a general implementation for an arbitrarily sized matrix.
 * <p>
 * Notes:
 * <ul>
 * <li>The matrix is represented as a one-dimensional <i>column major</i> array</li>
 * <li>The matrix {@link #order()} is determined at run-time on <b>every</b> invocation</li>
 * <li>Sub-classes should override the {@link #instance(float[])} method which is used to create new instances for matrix mutators, e.g. {@link #transpose()}</li>
 * <li>The copy constructor supports sub-class implementations and does <b>not</b> perform a defensive copy or validate the matrix order</li>
 * </ul>
 * @author Sarge
 */
public class DefaultMatrix implements Matrix {
	private final float[] matrix;

	/**
	 * Constructor.
	 * @param order			Matrix order
	 * @param matrix		Matrix elements (column-major)
	 * @throws IllegalArgumentException if the length of the matrix array does not match the specified order
	 */
	public DefaultMatrix(int order, float[] matrix) {
		this(matrix);
		if(matrix.length != order * order) throw new IllegalArgumentException(String.format("Invalid matrix length: order=%d len=%d", order, matrix.length));
	}
	// TODO - public? should be factory? used? i.e. only if creating matrix from persisting matrix?

	/**
	 * Copy constructor.
	 * @param matrix Matrix elements
	 */
	protected DefaultMatrix(float[] matrix) {
		this.matrix = notNull(matrix);
	}

	@Override
	public int order() {
		return switch(matrix.length) {
			case 0 -> 0;
			case 1 -> 1;
			case 4 -> 2;
			case 9 -> 3;
			case 16 -> 4;
			default -> (int) MathsUtil.sqrt(matrix.length);
		};
	}

	@Override
	public float[] array() {
		return Arrays.copyOf(matrix, matrix.length);
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

	@Override
	public float get(int row, int col) {
		final int index = Matrix.index(row, col, order());
		return matrix[index];
	}

	@Override
	public Vector row(int row) {
		final int order = order();
		final int index = Matrix.index(row, 0, order);
		return new Vector(matrix[index], matrix[index + order], matrix[index + 2 * order]);
	}

	@Override
	public Vector column(int col) {
		final int index = Matrix.index(0, col, order());
		return new Vector(matrix[index], matrix[index + 1], matrix[index + 2]);
	}

	/**
	 * Creates an new instance with the given matrix.
	 * This method should be overridden by sub-classes to support mutator methods.
	 * @param matrix Matrix
	 * @return New instance
	 */
	@SuppressWarnings("static-method")
	protected Matrix instance(float[] matrix) {
		return new DefaultMatrix(matrix);
	}

	@Override
	public Matrix transpose() {
		final float[] trans = new float[matrix.length];
		final int order = order();
		int index = 0;
		for(int r = 0; r < order; ++r) {
			for(int c = 0; c < order; ++c) {
				trans[index] = get(r, c);
				++index;
			}
		}
		return instance(trans);
	}

	@Override
	public Matrix multiply(Matrix m) {
		// Check same sized matrices
		final int order = order();
		if(m.order() != order) throw new IllegalArgumentException("Cannot multiply matrices with different sizes");

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
		return instance(result);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultMatrix that) &&
				(this.order() == that.order()) &&
				MathsUtil.isEqual(this.matrix, that.matrix);
	}

	private static final String LINE_SEPARATOR = System.lineSeparator();

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final int order = this.order();
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
}
