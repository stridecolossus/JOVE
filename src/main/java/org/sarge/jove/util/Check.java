package org.sarge.jove.util;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Parameter assertion methods.
 * @author Sarge
 */
public final class Check {
	private Check() {
		// Utility class
	}

	/**
	 * Checks that the given object is not {@code null}.
	 * @param obj Object to test
	 * @param msg Reason
	 * @throws IllegalArgumentException if the given object is <tt>null</tt>
	 */
	public static <T> T notNull(T obj, String msg) throws IllegalArgumentException {
		if(obj == null) {
            throw new IllegalArgumentException(msg);
        }
		return obj;
	}

	public static <T> T notNull(T obj) throws IllegalArgumentException {
		return notNull(obj, "Cannot be null");
	}

	/**
	 * Checks that the given string is not {@code null} or empty.
	 * @param str String to test
	 * @param msg Reason
	 * @throws IllegalArgumentException if the given string is empty
	 */
	public static String notEmpty(String str, String msg) throws IllegalArgumentException {
		if(StringUtils.isEmpty(str)) throw new IllegalArgumentException(msg);
		return str;
	}

	public static String notEmpty(String str) throws IllegalArgumentException {
		return notEmpty(str, "String cannot be empty");
	}

	/**
	 * Checks that the given collection is not {@code null} or empty.
	 * @param c Collection to test
	 * @param msg Reason
	 * @throws IllegalArgumentException if the given collection is empty
	 */
	public static <T> Collection<T> notEmpty(Collection<T> c, String msg) throws IllegalArgumentException {
		if((c == null) || c.isEmpty()) throw new IllegalArgumentException(msg);
		return c;
	}

	public static <T> Collection<T> notEmpty(Collection<T> c) throws IllegalArgumentException {
		return notEmpty(c, "Collection cannot be empty");
	}

	/**
	 * Checks that the given map is not {@code null} or empty.
	 * @param map Map
	 * @param msg Reason
	 * @return Map
	 * @throws IllegalArgumentException if the given map is empty
	 */
	public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String msg) throws IllegalArgumentException {
		if((map == null) || map.isEmpty()) throw new IllegalArgumentException(msg);
		return map;
	}

	public static <K, V> Map<K, V> notEmpty(Map<K, V> map) throws IllegalArgumentException {
		return notEmpty(map, "Map cannot be empty");
	}

	/**
	 * Checks that the given array is not {@code null} or empty.
	 * @param array Array to test
	 * @param <T> Array component type
	 * @throws IllegalArgumentException if the given array is empty
	 */
	@SafeVarargs
	public static <T> T[] notEmpty(T... array) throws IllegalArgumentException {
		if(array == null || (array.length == 0)) {
			throw new IllegalArgumentException("Array cannot be empty");
		}
		return array;
	}

	/**
     * Checks that the given number is zero-or-more.
     * @param <T> Number type
     * @param value Value to test
     */
    public static <T extends Number> T zeroOrMore(T value) {
        if(value.floatValue() < 0) {
            throw new IllegalArgumentException("Must be zero-or-more");
        }
        return value;
    }

    /**
     * Checks that the given number is one-or-more.
     * @param <T> Number type
     * @param value Value to test
     */
    public static <T extends Number> T oneOrMore(T value) {
        if(value.floatValue() < 1) {
            throw new IllegalArgumentException("Must be one-or-more");
        }
        return value;
    }

    /**
     * Checks that the given number is greater-than zero.
     * @param <T> Number type
     * @param value Value to test
     */
    public static <T extends Number> T positive(T value) {
    	if(value.floatValue() <= 0) {
    		throw new IllegalArgumentException("Must be positive and greater-than zero");
    	}
    	return value;
    }

    /**
     * Checks that the given number is within the specified range.
     * @param <T> Number type
     * @param value Value to test
     * @param min Minimum
     * @param max Maximum
     * @throws IllegalArgumentException if the value is outside of the specified range
     */
    public static <T extends Number> T range(T value, T min, T max) throws IllegalArgumentException {
    	final float f = value.floatValue();
        if((f < min.floatValue()) || (f > max.floatValue())) {
            throw new IllegalArgumentException("Value of out range: " + value + "(" + min + ".." + max + ")");
        }
        return value;
    }

	/**
	 * Checks that the given floating-point value is a valid 0..1 percentile.
	 * @param f Value to test
	 * @throws IllegalArgumentException if the value is not a percentile
	 */
	public static float isPercentile(float f) {
		return range(f, 0f, 1f);
	}
}
