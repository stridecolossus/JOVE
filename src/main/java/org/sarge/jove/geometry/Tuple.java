package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>tuple</i> is the base-class for 3-component floating-point values.
 * @author Sarge
 */
public sealed class Tuple implements Bufferable permits Point, Vector {
	/**
	 * Size of a tuple.
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
	 * @param tuple Tuple to copy
	 */
	protected Tuple(Tuple tuple) {
		this(tuple.x, tuple.y, tuple.z);
	}

	/**
	 * Array constructor.
	 * @param array Tuple array
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	protected Tuple(float[] array) {
		if(array.length != SIZE) throw new IllegalArgumentException("Invalid array length: " + array.length);
		x = array[0];
		y = array[1];
		z = array[2];
	}

	/**
	 * @return This tuple as an array
	 */
	public final float[] toArray() {
		final float[] array = new float[SIZE];
		array[0] = x;
		array[1] = y;
		array[2] = z;
		return array;
	}

	@Override
	public final void buffer(ByteBuffer buffer) {
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(z);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Tuple that) &&
				isEqual(that);
	}

	/**
	 * @param that Tuple
	 * @return Whether this and the given tuple are equal
	 */
	protected final boolean isEqual(Tuple that) {
		return
				MathsUtil.isEqual(this.x, that.x) &&
				MathsUtil.isEqual(this.y, that.y) &&
				MathsUtil.isEqual(this.z, that.z);
	}

	@Override
	public final String toString() {
		return Arrays.toString(toArray());
	}
}
