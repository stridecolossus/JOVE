package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.util.*;
import org.sarge.jove.util.FloatSupport.FloatFunction;
import org.sarge.lib.util.*;

/**
 * RGBA colour.
 * @author Sarge
 */
public record Colour(float red, float green, float blue, float alpha) implements Component, Bufferable {
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
	 * Size of a colour.
	 */
	public static final int SIZE = 4;

	/**
	 * Layout of a colour.
	 */
	public static final Layout LAYOUT = Layout.floats(SIZE);

	/**
	 * Colour converter.
	 */
	public static final Converter<Colour> CONVERTER = new FloatArrayConverter<>(SIZE, Colour::of);

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
	 * @throws IllegalArgumentException if any argument is not a 0..1 percentile value
	 * @see Percentile#isValid(float)
	 */
	public Colour {
		validate(red);
		validate(green);
		validate(blue);
		validate(alpha);
	}

	private static void validate(float f) {
		if(!Percentile.isValid(f)) throw new IllegalArgumentException("Invalid colour component: " + f);
	}

	/**
	 * Constructor with full alpha.
	 * @throws IllegalArgumentException if any argument is not a percentile value
	 */
	public Colour(float red, float green, float blue) {
		this(red, green, blue, 1);
	}

	/**
	 * Creates a colour interpolator.
	 * @param start				Start colour
	 * @param end				End colour
	 * @param interpolator		Interpolator function
	 * @return Colour interpolator
	 */
	public static FloatFunction<Colour> interpolator(Colour start, Colour end, Interpolator interpolator) {
		// Create an interpolator for each channel
		final float[] a = start.toArray();
		final float[] b = end.toArray();
		final Interpolator[] array = new Interpolator[SIZE];
		Arrays.setAll(array, n -> interpolator.range(a[n], b[n]));

		// Create colour interpolator
		return t -> {
			final float[] result = new float[SIZE];
			for(int n = 0; n < SIZE; ++n) {
				result[n] = array[n].apply(t);
			}
			return Colour.of(result);
		};
	}
	// TODO - move to separate class + loader?  see particle system loader
	// TODO - JDK19 vector API

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		buffer.putFloat(red);
		buffer.putFloat(green);
		buffer.putFloat(blue);
		buffer.putFloat(alpha);
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
