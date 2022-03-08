package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

/**
 * An <i>integer enumeration</i> is the base-class interface for an enumeration mapped to a native <b>typedef enum</b>.
 * <p>
 * An integer enumeration has a {@link ReverseMapping} that used to map integer literals to the corresponding enumeration constants.
 * <p>
 * Integer enumerations can be used in JNA methods and structures by registering the custom {@link CONVERTER} with the relevant JNA library.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create integer enumeration
 * enum Thing implements IntegerEnumeration {
 *     ONE(1),
 *     TWO(2),
 *     ...
 *
 *     private final int value;
 *
 *     public int value() {
 *         return value;
 *     }
 * }
 *
 * // Map literal to enumeration
 * ReverseMapping&lt;Thing&gt; mapping = IntegerEnumeration.mapping(Thing.class);
 * Thing thing = mapping.map(1);	// ONE
 *
 * // Build integer mask from enumeration
 * int mask = IntegerEnumeration.mask(Thing.ONE, ...);
 *
 * // Enumerate constants from integer mask
 * Set&lt;Thing&gt; set = mapping.enumerate(mask);
 * </pre>
 * <p>
 * @author Sarge
 */
public interface IntegerEnumeration {
	/**
	 * @return Enum literal
	 */
	int value();

	/**
	 * Retrieves the reverse mapping for the given integer enumeration.
	 * This method is thread-safe.
	 * @param <E> Integer enumeration
	 * @param clazz Enumeration class
	 * @return Reverse mapping
	 */
	static <E extends IntegerEnumeration> ReverseMapping<E> mapping(Class<E> clazz) {
		return ReverseMapping.get(clazz);
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Integer mask
	 */
	static <E extends IntegerEnumeration> int mask(Collection<E> values) {
		return values
				.stream()
				.mapToInt(IntegerEnumeration::value)
				.sum();
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Integer mask
	 */
	@SuppressWarnings("unchecked")
	static <E extends IntegerEnumeration> int mask(E... values) {
		return mask(Arrays.asList(values));
	}

	/**
	 * JNA type converter for an integer enumeration.
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

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			// Validate enumeration
			final Class<?> type = context.getTargetType();
			if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new RuntimeException("Invalid native enumeration class: " + type.getSimpleName());

			// Map native value
			final ReverseMapping<?> mapping = ReverseMapping.get(type);
			final int value = (int) nativeValue;
			if(value == 0) {
				return mapping.zero;
			}
			else {
				return mapping.map(value);
			}
		}
	};

	/**
	 * A <i>reverse mapping</i> is the inverse of an integer enumeration, i.e. maps native integers <i>to</i> the enumeration constants.
	 * @param <E> Integer enumeration
	 */
	final class ReverseMapping<E extends IntegerEnumeration> {
		private static final Map<Class<?>, ReverseMapping<?>> CACHE = new ConcurrentHashMap<>();

		/**
		 * Looks up the reverse mapping for the given enumeration.
		 * @param <E> Enumeration
		 * @param clazz Enumeration class
		 * @return Reverse mapping
		 */
		@SuppressWarnings("unchecked")
		private static <E extends IntegerEnumeration> ReverseMapping<E> get(Class<?> clazz) {
			return (ReverseMapping<E>) CACHE.computeIfAbsent(clazz, ReverseMapping::new);
		}

		private final Map<Integer, E> map;
		private final E zero;

		/**
		 * Constructor.
		 * @param clazz Integer enumeration class
		 */
		private ReverseMapping(Class<E> clazz) {
			final E[] array = clazz.getEnumConstants();
			this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));
			this.zero = map.getOrDefault(0, array[0]);
		}

		/**
		 * Maps an enumeration literal to the corresponding enumeration constant.
		 * @param clazz Enumeration class
		 * @param value Literal
		 * @return Constant
		 * @throws IllegalArgumentException if the enumeration does not contain the given value
		 */
		public E map(int value) {
			final E constant = map.get(value);
			if(constant == null) throw new IllegalArgumentException(String.format("Invalid enumeration literal: value=%d enum=%s", value, zero.getClass().getSimpleName()));
			return constant;
		}

		/**
		 * Converts a bit-field to an ordered set of enumeration constants.
		 * @param clazz		Enumeration class
		 * @param bits		Bit field
		 * @return Enumeration constants (in ascending order of value)
		 */
		public TreeSet<E> enumerate(int bits) {
			return new Mask(bits)
					.stream()
					.mapToObj(this::map)
					.collect(Collectors.toCollection(TreeSet::new));
		}
	}
}
