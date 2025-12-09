package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.*;

/**
 * Argument validation utilities.
 * @throws IllegalArgumentException for an invalid argument
 * @author Sarge
 */
public final class Validation {
	private Validation() {
	}

	/**
	 * Requires a non-empty string.
	 * @param string String
	 */
	public static String requireNotEmpty(String string) {
		requireNonNull(string);
		if(string.isEmpty()) throw new IllegalArgumentException();
		return string;
	}

	/**
	 * Requires a non-empty collection.
	 * @param collection Collection
	 */
	public static <T> Collection<T> requireNotEmpty(Collection<T> collection) {
		requireNonNull(collection);
		if(collection.isEmpty()) throw new IllegalArgumentException();
		return collection;
	}

	public static <T> T[] requireNotEmpty(T[] array) {
		requireNonNull(array);
		if(array.length == 0) throw new IllegalArgumentException();
		return array;
	}

	/**
	 * Requires a non-empty map.
	 * @param map Map
	 */
	public static <K, V> Map<K, V> requireNotEmpty(Map<K, V> map) {
		requireNonNull(map);
		if(map.isEmpty()) throw new IllegalArgumentException();
		return map;
	}

	/**
	 * Requires a zero-or-more integer.
	 * @param value Integer value
	 */
	public static int requireZeroOrMore(int value) {
		if(value < 0) throw new IllegalArgumentException();
		return value;
	}

	public static long requireZeroOrMore(long value) {
		if(value < 0) throw new IllegalArgumentException();
		return value;
	}

	public static float requireZeroOrMore(float value) {
		if(value < 0) throw new IllegalArgumentException();
		return value;
	}

	public static Duration requireZeroOrMore(Duration duration) {
		if(duration.isNegative()) throw new IllegalArgumentException();
		return duration;
	}

	/**
	 * Requires a one-or-more integer.
	 * @param value Integer value
	 */
	public static int requireOneOrMore(int value) {
		if(value < 1) throw new IllegalArgumentException();
		return value;
	}

	public static long requireOneOrMore(long value) {
		if(value < 1) throw new IllegalArgumentException();
		return value;
	}

	public static float requireOneOrMore(float value) {
		if(value < 1) throw new IllegalArgumentException();
		return value;
	}

	public static Duration requireOneOrMore(Duration duration) {
		if(!duration.isPositive()) throw new IllegalArgumentException();
		return duration;
	}

	/**
	 * Requires an integer value in the given range (inclusive).
	 * @param value 	Integer value
	 * @param min		Minimum value
	 * @param max		Maximum value
	 */
	public static int requireRange(int value, int min, int max) {
		if((value < min) || (value >= max)) {
			throw new IllegalArgumentException(String.format("Integer out-of-range: value=%d range=%d/%d", value, min, max));
		}
		return value;
	}
}
