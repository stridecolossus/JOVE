package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.util.Interpolator;
import org.sarge.lib.util.Check;

/**
 * RGBA colour.
 * @author Sarge
 */
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
	/**
	 * RGBA string.
	 */
	public static final String RGBA = "RGBA";

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

	/**
	 * Creates a colour from the given floating-point array representing an RGBA value <b>or</b> a 3-element RGB array with the alpha value initialised to <b>one</b>.
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
	 * @throws IllegalArgumentException if any argument is not a percentile value
	 */
	public Colour(float red, float green, float blue) {
		this(red, green, blue, 1);
	}

	/**
	 * Interpolates a colour between this and the given colour.
	 * @param col		Colour
	 * @param t			Amount
	 * @return Interpolated colour
	 */
	public Colour interpolate(Colour col, float t) {
		final float[] start = this.toArray();
		final float[] end = col.toArray();
		for(int n = 0; n < start.length; ++n) {
			start[n] = Interpolator.interpolate(t, start[n], end[n]);
		}
		return of(start);
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

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}
}
