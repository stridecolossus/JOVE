package org.sarge.jove.common;

import static org.sarge.jove.util.Check.isPercentile;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sarge.jove.util.Converter;
import org.sarge.jove.util.JoveUtil;
import org.sarge.jove.util.MathsUtil;

/**
 * RGBA colour.
 * @author Sarge
 */
public final class Colour implements Bufferable {
	/**
	 * Size of a colour.
	 */
	public static final int SIZE = 4;

	/**
	 * Material colour types.
	 */
	public enum Type {
		AMBIENT,
		DIFFUSE,
		SPECULAR,
	}

	/**
	 * White colour.
	 */
	public static final Colour WHITE = new Colour(1, 1, 1, 1);

	/**
	 * Black colour.
	 */
	public static final Colour BLACK = new Colour(0, 0, 0, 1);

	/**
	 * Colour converter.
	 */
	public static final Converter<Colour> CONVERTER = JoveUtil.converter(SIZE, Colour::new);

	private static final int MASK = 0xff;
	private static final float INV_MASK = 1f / MASK;

	/**
	 * Creates a colour from the given compacted pixel.
	 * @param pixel Pixel value
	 * @return Colour
	 */
	public static Colour of(int pixel) {
		final float a = mask(pixel >> 24);
		final float r = mask(pixel >> 16);
		final float g = mask(pixel >> 8);
		final float b = mask(pixel);
		return new Colour(r, g, b, a);
	}

	private static float mask(int pixel) {
		return (pixel & MASK) * INV_MASK;
	}

	public final float r, g, b, a;

	/**
	 * Constructor.
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @throws IllegalArgumentException if the arguments are not valid 0..1 RGBA components
	 */
	public Colour(float r, float g, float b, float a) {
		this.r = isPercentile(r);
		this.g = isPercentile(g);
		this.b = isPercentile(b);
		this.a = isPercentile(a);
	}

	/**
	 * Array constructor.
	 * @param array RGBA
	 * @throws IllegalArgumentException if the array does not contain valid RGBA components
	 */
	public Colour(float[] array) {
		if((array.length < 3) || (array.length > 4)) throw new IllegalArgumentException("Expected RGBA array components");
		r = isPercentile(array[0]);
		g = isPercentile(array[1]);
		b = isPercentile(array[2]);
		if(array.length == 4) {
			a = isPercentile(array[3]);
		}
		else {
			a = 1;
		}
	}

	@Override
	public void buffer(FloatBuffer buffer) {
		buffer.put(r).put(g).put(b).put(a);
	}

	/**
	 * Converts this colour to a compacted integer pixel value.
	 * @return Pixel value
	 */
	public int toPixel() {
		final int a = scale(this.a);
		final int r = scale(this.r);
		final int g = scale(this.g);
		final int b = scale(this.b);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int scale(float f) {
		return (int) (f * MASK);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof Colour) {
			final Colour that = (Colour) obj;
			if(!MathsUtil.equals(this.r, that.r)) return false;
			if(!MathsUtil.equals(this.g, that.g)) return false;
			if(!MathsUtil.equals(this.b, that.b)) return false;
			if(!MathsUtil.equals(this.a, that.a)) return false;
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
		return Arrays.toString(new float[]{r, g, b, a});
	}
}
