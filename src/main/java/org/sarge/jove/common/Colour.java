package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.io.Bufferable;
import org.sarge.lib.util.Check;

/**
 * RGBA colour.
 * @author Sarge
 */
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
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
	public static final Colour WHITE = new Colour(1, 1, 1);

	/**
	 * Black colour.
	 */
	public static final Colour BLACK = new Colour(0, 0, 0);

	/**
	 * Layout of a colour.
	 */
	public static final Layout LAYOUT = Layout.floats(4);

	private static final int MASK = 0xff;
	private static final float INV_MASK = 1f / MASK;

	/**
	 * Creates a colour from the given 4-element floating-point array representing an RGBA value <b>or</b> a 3-element RGB array where the alpha value is initialised to <b>one</b>.
	 * @param array Colour array
	 * @return New colour
	 * @throws IllegalArgumentException if the array is not an RGB or RGBA array or any component is not a valid percentile value
	 */
	public static Colour of(float[] array) {
		final float alpha = switch(array.length) {
			case 3 -> 1;
			case 4 -> array[3];
			default -> throw new IllegalArgumentException("Expected RGB(A) array components");
		};
		return new Colour(array[0], array[1], array[2], alpha);
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
	 * @throws IllegalArgumentException if any argument is not a percentile value
	 */
	public Colour {
		Check.isPercentile(red);
		Check.isPercentile(green);
		Check.isPercentile(blue);
		Check.isPercentile(alpha);
	}

	/**
	 * Constructor with full alpha.
	 */
	public Colour(float red, float green, float blue) {
		this(red, green, blue, 1);
	}

	@Override
	public int length() {
		return LAYOUT.length();
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
		final int a = mask(alpha);
		final int r = mask(red);
		final int g = mask(green);
		final int b = mask(blue);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	// TODO - some sort of pixel converter? pixel int to/from colour?

	private static int mask(float f) {
		return (int) (f * MASK);
	}

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}
}
