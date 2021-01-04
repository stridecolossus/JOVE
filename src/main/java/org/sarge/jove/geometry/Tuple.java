package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * Base-class for geometry with an XYZ tuple.
 * @author Sarge
 */
public class Tuple implements Bufferable {
	/**
	 * Size of a tuple.
	 */
	public static final int SIZE = 3;

	public final float x, y, z;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	protected Tuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Array constructor.
	 * @param array Tuple array
	 * @throws IllegalArgumentException if the given array does not have exactly three values
	 */
	protected Tuple(float[] array) {
		if(array.length != 3) throw new IllegalArgumentException("Invalid tuple array length: " + array.length);
		x = array[0];
		y = array[1];
		z = array[2];
	}

	/**
	 * Copy constructor.
	 * @param t Tuple
	 */
	protected Tuple(Tuple t) {
		this(t.x, t.y, t.z);
	}

	/**
	 * Calculates the dot (or scalar) product of this and the given tuple.
	 * @param t Tuple
	 * @return Dot product
	 */
	public final float dot(Tuple t) {
		// TODO - |v| * |u| * cos(angle)
		return x * t.x + y * t.y + z * t.z;
	}

	/**
	 * @return This tuple as an array
	 */
	public final float[] toArray() {
		return new float[]{x, y, z};
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		buffer.putFloat(x).putFloat(y).putFloat(z);
	}

	@Override
	public final long length() {
		return SIZE * Float.BYTES;
	}

	@Override
	public final boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		if(obj == null) {
			return false;
		}

		final Class<? extends Tuple> clazz = this.getClass();
		if(clazz == obj.getClass()) {
			final Tuple that = (Tuple) obj;
			return MathsUtil.isEqual(this.x, that.x) && MathsUtil.isEqual(this.y, that.y) && MathsUtil.isEqual(this.z, that.z);
		}
		else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public final String toString() {
		return Arrays.toString(toArray());
	}
}
