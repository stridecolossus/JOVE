package org.sarge.jove.common;

import static org.sarge.jove.util.Check.isPercentile;

import java.nio.ByteBuffer;
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
	public static final Converter<Colour> CONVERTER = JoveUtil.converter(SIZE, Colour::of);

	private static final int MASK = 0xff;
	private static final float INV_MASK = 1f / MASK;

	/**
	 * Creates a colour from the given floating-point array (either a 4-element RGBA array or 3-element RGB with alpha initialised to <b>one</b>)
	 * @param array Colour array
	 * @return New colour
	 * @throws IllegalArgumentException if the array is {@code null}, is not an RGB or RGBA array, or if any component is not a valid 0..1 colour value
	 */
	public static Colour of(float[] array) {
		if((array.length < 3) || (array.length > 4)) throw new IllegalArgumentException("Expected RGBA array components");
		final float r = isPercentile(array[0]);
		final float g = isPercentile(array[1]);
		final float b = isPercentile(array[2]);
		final float a = array.length == 3 ? 1 : isPercentile(array[3]);
		return new Colour(r, g, b, a);
	}

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

	@Override
	public long length() {
		return SIZE * Float.BYTES;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
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
