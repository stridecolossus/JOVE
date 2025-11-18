package org.sarge.jove.util;

import java.text.DecimalFormat;
import java.util.StringJoiner;

/**
 * Maths utilities.
 * <p>
 * This utility class provides:
 * <ul>
 * <li>General mathematics helper methods</li>
 * <li>Wrapper methods for square-root and inverse-root operations (should optimised implementations be required)</li>
 * <li>A suite of floating-point comparison operators that offer approximate equivalence, e.g. {@link #isApproxEqual(float, float)}</li>
 * </ul>
 * <p>
 * The accuracy of the approximation methods can be configured via the {@code jove.accuracy} system property.
 * <p>
 * @author Sarge
 */
public final class MathsUtility {
	/**
	 * Accuracy for floating-point comparisons.
	 */
	private static final float ACCURACY = Float.parseFloat(System.getProperty("jove.accuracy", "0.0001"));

	/**
	 * Convenience formatter for a floating-point value.
	 */
	public static final DecimalFormat FORMATTER = new DecimalFormat("#.####");

	/**
	 * Half value.
	 */
	public static final float HALF = 0.5f;

	/**
	 * PI (or 180 degrees).
	 */
	public static final float PI = (float) Math.PI;

	/**
	 * Half-PI (or 90 degrees).
	 */
	public static final float HALF_PI = PI / 2;

	/**
	 * Twice PI (or 360 degrees).
	 */
	public static final float TWO_PI = 2 * PI;

	private MathsUtility() {
	}

	/**
	 * Tests whether two floating-point values are approximately equal.
	 * TODO - infinity
	 * @return Whether the given values are approximately equal
	 */
	public static boolean isApproxEqual(float a, float b) {
		if(Float.isInfinite(a)) {
			return Float.isInfinite(b);
		}
		else {
			return isApproxZero(a - b);
		}
	}

	/**
	 * @param f Float
	 * @return Whether the given number is approximately zero
	 */
	public static boolean isApproxZero(float f) {
		return Math.abs(f) < ACCURACY;
	}

	/**
	 * @param n Number
	 * @return Whether the given integer is a power-of-two
	 */
	public static boolean isPowerOfTwo(int n) {
		if(n == 0) {
			return false;
		}
		else {
			return (n & (n - 1)) == 0;
		}
	}

	/**
	 * Calculates the maximum unsigned integer value for the given number of bits.
	 * @param bits Number of bits
	 * @return Maximum unsigned value
	 */
	public static long unsignedMaximum(int bits) {
		return (1L << bits) - 1;
	}

	/**
	 * Clamps the given floating-point value.
	 * @param value		Value to clamp
	 * @param min		Minimum
	 * @param max		Maximum
	 * @return Clamped value
	 */
	public static float clamp(float value, float min, float max) {
		if(value < min) {
			return min;
		}
		else
		if(value > max) {
			return max;
		}
		else {
			return value;
		}
	}

	/**
	 * Clamps the given floating-point value to a 0..1 percentile.
	 * @param value Value to saturate
	 * @return Saturated value
	 */
	public static float saturate(float value) {
		return clamp(value, 0, 1);
	}

	/**
	 * @param radians Angle in radians
	 * @return Angle in degrees
	 */
	public static float toDegrees(float radians) {
		return (float) Math.toDegrees(radians);
	}

	/**
	 * @param degrees Angle in degrees
	 * @return Angle in radians
	 */
	public static float toRadians(float degrees) {
		return (float) Math.toRadians(degrees);
	}

	/**
	 * @param f Value
	 * @return Square root
	 */
	public static float sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	/**
	 * @param f Value
	 * @return Inverse square root
	 */
	public static float inverseSquareRoot(float f) {
		return 1 / sqrt(f);
	}

	/**
	 * Formats an array of floating-point values as a comma-separated string.
	 * @param values Floating-point values
	 * @return Formatted array
	 * @see #FORMATTER
	 */
	public static String toString(float... values) {
		final var str = new StringJoiner(", ");
		for(float f : values) {
			str.add(FORMATTER.format(f));
		}
		return str.toString();
	}
}
