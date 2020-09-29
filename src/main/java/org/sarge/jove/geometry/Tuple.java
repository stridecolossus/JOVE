package org.sarge.jove.geometry;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.Converter;
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

	/**
	 * A <i>swizzle</i> is used to swap tuple values.
	 */
	public enum Swizzle implements UnaryOperator<Tuple> {
		NONE,
		XY,
		XZ,
		YZ;

		/**
		 * Swizzle string converter.
		 */
		public static final Converter<Swizzle> CONVERTER = Converter.enumeration(Swizzle.class);

		@Override
		public Tuple apply(Tuple t) {
			switch(this) {
			case NONE:		return t;
			case XY: 		return new Tuple(t.y, t.x, t.z);
			case XZ: 		return new Tuple(t.z, t.y, t.x);
			case YZ: 		return new Tuple(t.x, t.z, t.y);
			default: 		throw new RuntimeException();
			}
		}
	}

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
	public float dot(Tuple t) {
		// TODO - |v| * |u| * cos(angle)
		return x * t.x + y * t.y + z * t.z;
	}

	/**
	 * @return This tuple as an array
	 */
	public float[] toArray() {
		return new float[]{x, y, z};
	}

	@Override
	public void buffer(FloatBuffer buffer) {
		buffer.put(x).put(y).put(z);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		final Class<? extends Tuple> clazz = this.getClass();
		if(clazz == obj.getClass()) {
			final Tuple that = (Tuple) obj;
			if(!MathsUtil.equals(this.x, that.x)) return false;
			if(!MathsUtil.equals(this.y, that.y)) return false;
			if(!MathsUtil.equals(this.z, that.z)) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}
}
