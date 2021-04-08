package org.sarge.jove.common;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * An <i>integer enumeration</i> is a base-class interface for an enumeration mapped to a native <code>typedef enum</code>.
 * @author Sarge
 */
public interface IntegerEnumeration {
	/**
	 * @return Enum literal
	 */
	int value();

	/**
	 * Integer <i>or</i> binary operator used to reduce a stream of masked integer values.
	 */
	IntBinaryOperator MASK = (a, b) -> a | b;

	/**
	 * Converts an integer enumeration to/from a native <code>int</code> value.
	 */
	TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return 0;
			}
			else {
				final IntegerEnumeration e = (IntegerEnumeration) value;
				return e.value();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			// Lookup enumeration
			final Class<?> type = context.getTargetType();
			if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new IllegalStateException("Invalid native enumeration class: " + type.getSimpleName());
			final var entry = Cache.CACHE.get((Class<? extends IntegerEnumeration>) type);

			// Map native value
			final int value = (int) nativeValue;
			if(value == 0) {
				return entry.zero();
			}
			else {
				return entry.get(value);
			}
		}
	};

	/**
	 * Maps an enumeration literal to the corresponding enumeration constant.
	 * @param clazz Enumeration class
	 * @param value Literal
	 * @return Constant
	 * @throws IllegalArgumentException if the enumeration does not contain the given value
	 */
	static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) {
		return Cache.CACHE.get(clazz).get(value);
	}

	/**
	 * Converts an integer mask to a set of enumeration constants.
	 * @param clazz		Enumeration class
	 * @param mask		Mask
	 * @return Enumeration constants (in ascending order of value)
	 */
	static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) {
		final var entry = Cache.CACHE.get(clazz);
		final Set<E> values = new TreeSet<>();
		final int max = Integer.highestOneBit(mask);
		for(int n = 0; n < max; ++n) {
			final int value = 1 << n;
			if((value & mask) == value) {
				values.add(entry.get(value));
			}
		}
		return values;
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 */
	static <E extends IntegerEnumeration> int mask(Collection<E> values) {
		return values.stream().distinct().mapToInt(IntegerEnumeration::value).reduce(0, MASK);
	}
	// TODO - set? therefore can assume distinct

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 */
	@SafeVarargs
	static <E extends IntegerEnumeration> int mask(E... values) {
		return mask(Arrays.asList(values));
	}

	/**
	 * Internal enumeration cache.
	 */
	final class Cache {
		/**
		 * Singleton instance.
		 */
		private static final Cache CACHE = new Cache();

		/**
		 * Cache entry.
		 */
		private class Entry {
			private final Map<Integer, ? extends IntegerEnumeration> map;
			private final Object zero;

			/**
			 * Constructor.
			 * @param clazz Enumeration class
			 */
			private Entry(Class<? extends IntegerEnumeration> clazz) {
				// Build reverse mapping
				final IntegerEnumeration[] array = clazz.getEnumConstants();
				this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));

				// Determine zero value
				final Object def = map.get(0);
				this.zero = def == null ? array[0] : def;
			}

			/**
			 * Looks up the enumeration constant for the given value.
			 * @param <E> Enumeration
			 * @param value Constant value
			 * @return Enumeration constant
			 * @throws IllegalArgumentException for an unknown value
			 */
			@SuppressWarnings("unchecked")
			private <E extends IntegerEnumeration> E get(int value) {
				final E result = (E) map.get(value);
				if(result == null) {
					final Class<?> clazz = map.values().iterator().next().getClass();
					throw new IllegalArgumentException(String.format("Unknown enumeration value: enum=%s value=%d", clazz.getSimpleName(), value));
				}
				return result;
			}

			/**
			 * Zero (or default) value for this enumeration.
			 * @param <E> Enumeration
			 * @return Zero value
			 */
			@SuppressWarnings("unchecked")
			private <E extends IntegerEnumeration> E zero() {
				return (E) zero;
			}
		}

		private final Map<Class<? extends IntegerEnumeration>, Entry> cache = new ConcurrentHashMap<>();

		private Cache() {
		}

		/**
		 * Looks up a cache entry for the given enumeration.
		 * @param clazz Enumeration class
		 * @return Cache entry
		 */
		private Entry get(Class<? extends IntegerEnumeration> clazz) {
			return cache.computeIfAbsent(clazz, Entry::new);
		}
	}
}
