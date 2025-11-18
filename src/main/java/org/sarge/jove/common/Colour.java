package org.sarge.jove.common;

import java.nio.ByteBuffer;

import org.sarge.jove.util.MathsUtility;

/**
 * RGBA colour.
 * @author Sarge
 */
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
	/**
	 * RGBA string.
	 */
	public static final String RGBA = "RGBA";
	// TODO - not here?

	/**
	 * White colour.
	 */
	public static final Colour WHITE = new Colour(1, 1, 1);

	/**
	 * Black colour.
	 */
	public static final Colour BLACK = new Colour(0, 0, 0);

	/**
	 * Number of colour components.
	 */
	public static final int SIZE = 4;

	/**
	 * Layout of a colour.
	 */
	public static final Layout LAYOUT = Layout.floats(SIZE);

	/**
	 * Creates a colour from the given floating-point array representing an RGBA colour <b>or</b> an RGB colour with the alpha value initialised to <b>one</b>.
	 * @param array Colour array
	 * @return Colour
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
	 * Parses a colour from the given comma-separate string.
	 * @param colour Colour string
	 * @return Colour
	 * @throws NumberFormatException if any component is not a valid floating-point value
	 * @see #of(float[])
	 */
	public static Colour parse(String colour) {
		final String[] parts = colour.split(",");
		final float[] array = new float[parts.length];
		for(int n = 0; n < parts.length; ++n) {
			array[n] = Float.parseFloat(parts[n]);
		}
		return of(array);
	}

	/**
	 * Constructor.
	 * @throws IllegalArgumentException if any argument is not a 0..1 percentile value
	 */
	public Colour {
		validate(red);
		validate(green);
		validate(blue);
		validate(alpha);
	}

	private static void validate(float value) {
		if((value < 0) || (value > 1)) throw new IllegalArgumentException();
	}

	/**
	 * Constructor with full alpha.
	 * @see #Colour(float, float, float, float)
	 */
	public Colour(float red, float green, float blue) {
		this(red, green, blue, 1);
	}

//	/**
//	 * Creates a colour interpolator.
//	 * @param start				Start colour
//	 * @param end				End colour
//	 * @param interpolator		Interpolator function
//	 * @return Colour interpolator
//	 */
//	public static FloatFunction<Colour> interpolator(Colour start, Colour end, Interpolator interpolator) {
//		// Create an interpolator for each channel
//		final float[] a = start.toArray();
//		final float[] b = end.toArray();
//		final Interpolator[] array = new Interpolator[SIZE];
//		Arrays.setAll(array, n -> interpolator.range(a[n], b[n]));
//
//		// Create colour interpolator
//		return t -> {
//			final float[] result = new float[SIZE];
//			for(int n = 0; n < SIZE; ++n) {
//				result[n] = array[n].apply(t);
//			}
//			return Colour.of(result);
//		};
//	}
//	// TODO - move to separate class + loader?  see particle system loader
//	// TODO - JDK19 vector API

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
		return MathsUtility.format(toArray());
	}
}
