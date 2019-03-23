package org.sarge.jove.platform;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * An <i>integer enumeration</i> is used to define an enumeration mapped to a native <tt>typedef enum</tt>.
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
	 * Converts an integer enumeration to/from a native <tt>int</tt> value.
	 * @see <a href="http://technofovea.com/blog/archives/815">enumeration type converter example</a>
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
			final Class<?> type = context.getTargetType();
			if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new IllegalStateException("Invalid native enumeration class: " + type.getSimpleName());
			final Class<? extends IntegerEnumeration> clazz = (Class<? extends IntegerEnumeration>) type;
			return Cache.CACHE.get(clazz).get((int) nativeValue);
		}
	};

	/**
	 * Maps an enumeration literal to the corresponding enumeration constant.
	 * @param clazz Enumeration class
	 * @param value Literal
	 * @return Constant
	 * @throws IllegalArgumentException if the enumeration does not contain the given value
	 */
	public static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) {
		final Cache.Entry entry = Cache.CACHE.get(clazz);
		return entry.get(value);
	}

	/**
	 * Tests whether an integer mask contains the given enumeration value.
	 * @param mask			Mask
	 * @param constant		Enumeration constant
	 * @return Whether is present
	 */
	public static <E extends IntegerEnumeration> boolean contains(int mask, E constant) {
		return (constant.value() & mask) != 0;
	}

	/**
	 * Converts an integer mask to a set of enumeration constants.
	 * @param clazz		Enumeration class
	 * @param mask		Mask
	 * @return Constants
	 */
	public static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) {
		final Cache.Entry entry = Cache.CACHE.get(clazz);
		final Set<E> set = new HashSet<>();
		final int max = Integer.highestOneBit(mask);
		for(int n = 0; n < max; ++n) {
			final int value = 1 << n;
			if((value & mask) == value) {
				set.add(entry.get(value));
			}
		}
		return set;
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 * @see #MASK
	 */
	public static <E extends IntegerEnumeration> int mask(Collection<E> values) {
		return values.stream().distinct().mapToInt(IntegerEnumeration::value).reduce(0, MASK);
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 */
	@SuppressWarnings("unchecked")
	public static <E extends IntegerEnumeration> int mask(E... values) {
		return mask(Arrays.asList(values));
	}

	/**
	 * Internal enumeration cache.
	 */
	final class Cache {
		private static final Cache CACHE = new Cache();

		/**
		 * Cache entry.
		 */
		private class Entry {
			private final Class<? extends IntegerEnumeration> clazz;
			private final IntegerEnumeration[] values;
			private final Map<Integer, IntegerEnumeration> map;

			/**
			 * Constructor.
			 * @param clazz Enumeration class
			 */
			private Entry(Class<? extends IntegerEnumeration> clazz) {
				this.clazz = notNull(clazz);
				this.values = clazz.getEnumConstants();
				this.map = Arrays.stream(values).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));
			}

			/**
			 * Looks up an integer enumeration constant by value.
			 * @param value Value
			 * @return Enumeration constant
			 */
			<E extends IntegerEnumeration> E get(int value) {
				@SuppressWarnings("unchecked")
				final E result = (E) map.get(value);
				if(result == null) throw new IllegalArgumentException(String.format("Unknown enumeration value: enum=%s value=%d", clazz.getSimpleName(), value));
				return result;
			}
		}

		private final Map<Class<? extends IntegerEnumeration>, Entry> cache = new ConcurrentHashMap<>();

		private Cache() {
		}

		/**
		 * Looks up or creates a cache entry for the given enumeration class.
		 * @param clazz Enumeration class
		 * @return Cache entry
		 */
		Entry get(Class<? extends IntegerEnumeration> clazz) {
			return cache.computeIfAbsent(clazz, Entry::new);
		}
	}
}
