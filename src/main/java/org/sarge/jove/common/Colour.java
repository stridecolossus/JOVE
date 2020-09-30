package org.sarge.jove.common;

import static org.sarge.jove.util.Check.isPercentile;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.sarge.jove.util.Check;
import org.sarge.jove.util.Converter;
import org.sarge.jove.util.JoveUtil;

/**
 * RGBA colour.
 * @author Sarge
 */
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
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

	/**
	 * Constructor.
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @throws IllegalArgumentException if the arguments are not valid 0..1 RGBA components
	 */
	public Colour {
		Check.isPercentile(red);
		Check.isPercentile(green);
		Check.isPercentile(blue);
		Check.isPercentile(alpha);
	}

	/**
	 * Array constructor.
	 * @param array RGBA
	 * @throws IllegalArgumentException if the array does not contain valid RGBA components
	 */
	public Colour(float[] array) {
		if((array.length < 3) || (array.length > 4)) throw new IllegalArgumentException("Expected RGBA array components");
		red = isPercentile(array[0]);
		green = isPercentile(array[1]);
		blue = isPercentile(array[2]);
		if(array.length == 4) {
			alpha = isPercentile(array[3]);
		}
		else {
			alpha = 1;
		}
	}

	@Override
	public void buffer(FloatBuffer buffer) {
		buffer.put(red).put(green).put(blue).put(alpha);
	}

	/**
	 * @return This colour as an RGBA array of floating-point values
	 */
	public float[] toArray() {
		return new float[]{red, green, blue, alpha};
	}

	/**
	 * Converts this colour to a compacted integer pixel value.
	 * @return Pixel value
	 */
	public int toPixel() {
		final int a = scale(this.alpha);
		final int r = scale(this.red);
		final int g = scale(this.green);
		final int b = scale(this.blue);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int scale(float f) {
		return (int) (f * MASK);
	}

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}
}
