package org.sarge.jove.util;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.sun.jna.*;

/**
 * An <i>integer enumeration</i> is a base-class interface for an enumeration mapped to a native <b>typedef enum</b>.
 * <p>
 * An integer enumeration has a {@link ReverseMapping} which maps integer literals to the corresponding enumeration constants.
 * <p>
 * Integer enumerations can be used in JNA methods and structures by registering the custom {@link #CONVERTER} with the relevant JNA library.
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
 * // Build integer bit-field from enumeration
 * int bits = IntegerEnumeration.reduce(Thing.ONE, ...);
 *
 * // Enumerate constants from integer bit-field
 * Set set = mapping.enumerate(bits);
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
	 * Builds a bit-field from the given enumeration constants.
	 * @param values Enumeration constants
	 * @param <E> Enumeration
	 * @return Bit-field
	 */
	static <E extends IntegerEnumeration> int reduce(Collection<E> values) {
		return values
				.stream()
				.mapToInt(IntegerEnumeration::value)
				.reduce(0, (a, b) -> a | b);
	}

	/**
	 * Builds a bit-field from the given enumeration constants.
	 * @param values Enumeration constants
	 * @param <E> Enumeration
	 * @return Bit-field
	 */
	@SuppressWarnings("unchecked")
	static <E extends IntegerEnumeration> int reduce(E... values) {
		return reduce(Set.of(values));
	}

	/**
	 * Retrieves the reverse mapping for the given integer enumeration.
	 * This method is thread-safe.
	 * @param <E> Enumeration
	 * @param clazz Enumeration class
	 * @return Reverse mapping
	 */
	@SuppressWarnings("unchecked")
	static <E extends IntegerEnumeration> ReverseMapping<E> reverse(Class<E> clazz) {
		return (ReverseMapping<E>) ReverseMapping.get(clazz);
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
			if(value instanceof IntegerEnumeration e) {
				return e.value();
			}
			else {
				return 0;
			}
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			// Validate enumeration
			final Class<?> type = context.getTargetType();
			if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new RuntimeException("Invalid native enumeration class: " + type.getSimpleName());

			// Map native value
			final ReverseMapping<?> mapping = ReverseMapping.get(type);
			if(nativeValue == null) {
				return mapping.def;
			}
			else {
				final int num = (int) nativeValue;
				if(num == 0) {
					return mapping.def;
				}
				else {
					return mapping.map(num);
				}
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
		private static ReverseMapping<?> get(Class<?> clazz) {
			return CACHE.computeIfAbsent(clazz, ReverseMapping::new);
		}

		private final Map<Integer, E> map;
		private final E def;

		/**
		 * Constructor.
		 * @param clazz Integer enumeration class
		 */
		private ReverseMapping(Class<E> clazz) {
			final E[] array = clazz.getEnumConstants();
			this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));
			this.def = map.getOrDefault(0, array[0]);
		}

		/**
		 * Maps an enumeration literal to the corresponding enumeration constant.
		 * @param value Literal
		 * @return Constant
		 * @throws IllegalArgumentException if the enumeration does not contain the given value
		 */
		public E map(int value) {
			final E constant = map.get(value);
			if(constant == null) throw new IllegalArgumentException(String.format("Invalid enumeration literal: value=%d enum=%s", value, def.getClass().getSimpleName()));
			return constant;
		}

		/**
		 * Converts a bit-field to a set of enumeration constants.
		 * @param bits Bit field
		 * @return Enumeration constants
		 */
		public Set<E> enumerate(int bits) {
			return new Mask(bits)
					.stream()
					.mapToObj(this::map)
					.collect(toSet());
		}
	}
}
