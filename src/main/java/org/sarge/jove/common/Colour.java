package org.sarge.jove.common;

import java.nio.FloatBuffer;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.HashCodeBuilder;
import org.sarge.lib.util.ToString;

/**
 * RGBA colour.
 * @author Sarge
 */
public final class Colour implements Bufferable {
	/**
	 * Number of floating-point values in a colour.
	 */
	public static final int SIZE = 4;

	/**
	 * Converts a comma-delimited RGBA string to a colour.
	 */
	public static Converter<Colour> CONVERTER = str -> new Colour(MathsUtil.convert(str, 4));

	/**
	 * White.
	 */
	public static final Colour WHITE = new Colour(1, 1, 1, 1);

	/**
	 * Black.
	 */
	public static final Colour BLACK = new Colour(0, 0, 0, 1);

	public final float r, g, b, a;

	/**
	 * Constructor.
	 * @param r Red
	 * @param g Green
	 * @param b Blue
	 * @param a Alpha
	 */
	public Colour(float r, float g, float b, float a) {
		Check.isPercentile(r);
		Check.isPercentile(g);
		Check.isPercentile(b);
		Check.isPercentile(a);

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	/**
	 * Constructor given an array.
	 * @param array Colour as an array
	 */
	public Colour(float[] array) {
		this(array[0], array[1], array[2], array[3]);
	}

	@Override
	public int getComponentSize() {
		return SIZE;
	}

	@Override
	public void append(FloatBuffer buffer) {
		buffer
			.put(r)
			.put(g)
			.put(b)
			.put(a);
	}

	/**
	 * Converts this colour to an array.
	 * @param array Colour array
	 */
	public void toArray(float[] array) {
		array[0] = r;
		array[1] = g;
		array[2] = b;
		array[3] = a;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof Colour) {
			final Colour col = (Colour) obj;
			if(!MathsUtil.isEqual(this.r, col.r)) return false;
			if(!MathsUtil.isEqual(this.g, col.g)) return false;
			if(!MathsUtil.isEqual(this.b, col.b)) return false;
			if(!MathsUtil.isEqual(this.a, col.a)) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.hashCode(this);
	}

	@Override
	public String toString() {
		return ToString.toString(r, g, b, a);
	}
}
