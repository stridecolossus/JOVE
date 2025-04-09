package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>tuple</i> is the base class for 3D points and vectors.
 * @author Sarge
 */
class Tuple implements Bufferable {
	/**
	 * Number of components in a tuple.
	 */
	public static final int SIZE = 3;

	public final float x, y, z;

	/**
	 * Constructor.
	 */
	protected Tuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Copy constructor.
	 * @param that Tuple to copy
	 */
	protected Tuple(Tuple that) {
		this(that.x, that.y, that.z);
	}

	/**
	 * Array constructor.
	 * @param array Tuple array
	 * @throws ArrayIndexOutOfBoundsException if the array does not contain at least three elements
	 */
	protected Tuple(float[] array) {
		this(array[0], array[1], array[2]);
	}

	/**
	 * @return This tuple as an array
	 */
	public float[] toArray() {
		return new float[]{x, y, z};
	}

	@Override
	public final void buffer(ByteBuffer buffer) {
		buffer
				.putFloat(x)
				.putFloat(y)
				.putFloat(z);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

	// TODO - static?
	protected final boolean isEqual(Tuple that) {
		return
				MathsUtility.isApproxEqual(this.x, that.x) &&
				MathsUtility.isApproxEqual(this.y, that.y) &&
				MathsUtility.isApproxEqual(this.z, that.z);
	}

	@Override
	public String toString() {
		return MathsUtility.format(x, y, z);
	}
}
