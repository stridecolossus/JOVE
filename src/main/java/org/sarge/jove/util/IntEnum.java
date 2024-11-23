package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An <i>integer enumeration</i> is a base-class interface for an enumeration mapped to a native <b>typedef enum</b>.
 * <p>
 * An integer enumeration has a {@link ReverseMapping} which maps integer literals to the corresponding enumeration constants.
 * <p>
 * Example usage:
 * <p>
 * {@snippet lang="java" :
 * // Create integer enumeration
 * enum Thing implements IntEnum {
 *     ONE(1),
 *     TWO(2),
 *     ...
 *
 *     private final int value;
 *
 *     @Override
 *     public int value() {
 *         return value;
 *     }
 * }
 *
 * // Map literal to enumeration
 * ReverseMapping<Thing> mapping = IntEnum.mapping(Thing.class);
 * Thing one = mapping.map(1);
 * <p>
 * Integer enumerations can be used in JNA methods and structures by registering the custom {@link #CONVERTER} with the relevant JNA library.
 * <p>
 * @see BitMask
 * @author Sarge
 */
public interface IntEnum {
	/**
	 * @return Enum literal
	 */
	int value();

	/**
	 * Retrieves the reverse mapping for the given integer enumeration.
	 * This method is thread-safe.
	 * @param <E> Enumeration
	 * @param type Enumeration class
	 * @return Reverse mapping
	 */
	@SuppressWarnings("unchecked")
	static <E extends IntEnum> ReverseMapping<E> reverse(Class<E> type) {
		return (ReverseMapping<E>) ReverseMapping.get(type);
	}

	// TODO - get rid of static / thread-safe cache?
	// used by:
	// - mapper / structures => cache locally without need for locking
	// - custom use-cases, e.g. physical device queue flags (?) => one-off

	/**
	 * A <i>reverse mapping</i> is the inverse of an integer enumeration, i.e. maps native integers <i>to</i> the enumeration constants.
	 * Note that constants with duplicate values within an enumeration are silently ignored by this implementation.
	 * @param <E> Integer enumeration
	 */
	public final class ReverseMapping<E extends IntEnum> {
		private static final Map<Class<?>, ReverseMapping<?>> CACHE = new ConcurrentHashMap<>();

		/**
		 * Looks up the reverse mapping for the given enumeration.
		 * This method is thread-safe.
		 * @param <E> Enumeration
		 * @param type Enumeration class
		 * @return Reverse mapping
		 */
		@SuppressWarnings("unchecked")
		public static ReverseMapping<?> get(Class<?> type) {

			// TODO
			if(type == IntEnum.class) throw new IllegalArgumentException();

			return CACHE.computeIfAbsent(type, ReverseMapping::new);
		}

		private final Map<Integer, E> map;
		private final E def;

		/**
		 * Constructor.
		 * @param type Integer enumeration class
		 */
		private ReverseMapping(Class<E> type) {
			final E[] array = type.getEnumConstants();
			this.map = Arrays.stream(array).collect(toMap(IntEnum::value, Function.identity(), (a, b) -> a));
			this.def = map.getOrDefault(0, array[0]);
		}

		/**
		 * @return Default value for this enumeration
		 */
		public E defaultValue() {
			return def;
		}

		/**
		 * Maps an enumeration literal to the corresponding enumeration constant.
		 * @param value Native literal
		 * @return Constant
		 * @throws IllegalArgumentException if the enumeration does not contain the given value
		 */
		public E map(int value) {
			final E constant = map.get(value);
			if(constant == null) {
				throw new IllegalArgumentException(String.format("Invalid enumeration literal: value=%d enum=%s", value, def.getClass()));
			}
			return constant;
		}
	}
}
