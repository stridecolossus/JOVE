package org.sarge.jove.util;

/**
 * Maths utilities.
 * <p>
 * This class provides a suite of floating-point comparison operators that offer approximate equivalence, e.g. {@link #isEqual(float, float)}.
 * The accuracy of these methods can be configured via the {@code jove.accuracy} system property.
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

	/**
	 * PI (or 180 degrees).
	 */
	public static final float PI = (float) Math.PI;

	/**
	 * Half-PI (or 90 degrees).
	 */
	public static final float HALF_PI = PI * HALF;

	/**
	 * Double PI (or 360 degrees).
	 */
	public static final float TWO_PI = 2 * PI;

	/**
	 * Converts degrees to radians.
	 */
	private static final float DEGREES_TO_RADIANS = PI / 180;

	/**
	 * Converts radians to degrees.
	 */
	private static final float RADIANS_TO_DEGREES = 1 / DEGREES_TO_RADIANS;

	private MathsUtil() {
	}

	/**
	 * Tests whether two floating-point values are approximately equal.
	 * TODO - infinity
	 * @param a
	 * @param b
	 * @return Whether the given values are approximately equal
	 */
	public static boolean isEqual(float a, float b) {
		if(Float.isInfinite(a)) {
			return Float.isInfinite(b);
		}
		else {
			return Math.abs(a - b) < ACCURACY;
		}
	}

	/**
	 * Tests whether two floating-point arrays are approximately equal.
	 * @param a
	 * @param b
	 * @return Whether the given floating-point arrays are approximately equal
	 * @throws NullPointerException if either array is {@code null}
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
	 * @return Whether the given integer is even
	 */
	public static boolean isEven(int num) {
		return (num % 2) == 0;
	}

	/**
	 * @param value		Value
	 * @param mask		Bit-wise mask
	 * @return Whether the given value matches the specified bit-wise mask
	 */
	public static boolean isMask(int value, int mask) {
		return (value & mask) == value;
	}

	/**
	 * @param mask		Mask
	 * @param bit		Bit index
	 * @return Whether the specified bit is set in the given integer mask
	 */
	public static boolean isBit(int mask, int bit) {
		return isMask(1 << bit, mask);
	}

	/**
	 * @param num Number
	 * @return Whether the given integer is a power-of-two
	 */
	public static boolean isPowerOfTwo(int num) {
		return (num > 0) && (num & (num - 1)) == 0;
	}

	/**
	 * Calculates the maximum unsigned integer value comprising the given number of bits.
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
	 * @param radians Angle in radians
	 * @return Angle in degrees
	 */
	public static float toDegrees(float radians) {
		return radians * RADIANS_TO_DEGREES;
	}

	/**
	 * @param degrees Angle in degrees
	 * @return Angle in radians
	 */
	public static float toRadians(float degrees) {
		return degrees * DEGREES_TO_RADIANS;
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

	/**
	 * @param angle Angle (radians)
	 * @return Sine of the given angle
	 * @see <a href="https://en.wikipedia.org/wiki/Sine">Wikipedia</a>
	 */
	public static float sin(float angle) {
		return (float) Math.sin(angle);
	}

	/**
	 * @param angle Angle (radians)
	 * @return Cosine of the given angle
	 * @see <a href="https://en.wikipedia.org/wiki/Sine">Wikipedia</a>
	 */
	public static float cos(float angle) {
		// TODO - return sin(angle + HALF_PI); ???
		return (float) Math.cos(angle);
	}

	/**
	 * @param angle Angle (radians)
	 * @return Tangent of the given angle
	 */
	public static float tan(float angle) {
		return (float) Math.tan(angle);
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-sine of the given angle
	 */
	public static float asin(float angle) {
		if(angle > -1f) {
			if(angle < 1f) {
				return (float) Math.asin(angle);
			}
			else {
				return HALF_PI;
			}
		}
		else {
			return -HALF_PI;
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-cosine of the given angle
	 */
	public static float acos(float angle) {
		if(angle > -1f) {
			if(angle < 1f) {
				return (float) Math.acos(angle);
			}
			else {
				return 0f;
			}
		}
		else {
			return PI;
		}
	}

	/**
	 * @param angle Angle (radians)
	 * @return Arc-tangent of the given angle
	 */
	public static float atan(float angle) {
		return (float) Math.atan(angle);
	}
}
