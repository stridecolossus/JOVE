package org.sarge.jove.util;

/**
 * Maths utilities.
 * <p>
 * This utility class provides:
 * <ul>
 * <li>General mathematics helper methods</li>
 * <li>Wrapper methods for square-root and inverse-root operations (should optimised implementations be required)</li>
 * <li>A suite of floating-point comparison operators that offer approximate equivalence, e.g. {@link #isEqual(float, float)}</li>
 * </ul>
 * <p>
 * The accuracy of the approximation methods can be configured via the {@code jove.accuracy} system property.
 * <p>
 * @author Sarge
 */
public final class MathsUtil {
	/**
	 * Accuracy for floating-point comparisons.
	 */
	private static final float ACCURACY = Float.parseFloat(System.getProperty("jove.accuracy", "0.0001"));

	/**
	 * Half value.
	 */
	public static final float HALF = 0.5f;

	private MathsUtil() {
	}

	/**
	 * Tests whether two floating-point values are approximately equal.
	 * TODO - infinity
	 * @return Whether the given values are approximately equal
	 */
	public static boolean isEqual(float a, float b) {
		if(Float.isInfinite(a)) {
			return Float.isInfinite(b);
		}
		else {
			return isZero(a - b);
		}
	}

	/**
	 * @return Whether the given floating-point arrays are approximately equal
	 */
	public static boolean isEqual(float[] a, float[] b) {
		if(a.length != b.length) {
			return false;
		}

		for(int n = 0; n < a.length; ++n) {
			if(!isEqual(a[n], b[n])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param f Float
	 * @return Whether the given number is approximately zero
	 */
	public static boolean isZero(float f) {
		return Math.abs(f) < ACCURACY;
	}

	/**
	 * @param num Number
	 * @return Whether the given integer is a power-of-two
	 */
	public static boolean isPowerOfTwo(int num) {
		if(num == 0) {
			return false;
		}
		else {
			return (num & (num - 1)) == 0;
		}
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
	public static float inverseRoot(float f) {
		return 1 / sqrt(f);
	}
}
