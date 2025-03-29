package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.sarge.jove.foreign.NativeTransformer;

/**
 * An <i>integer enumeration</i> is a type definition for an enumeration mapped to a native <b>typedef enum</b>.
 * <p>
 * An integer enumeration can be treated as bi-directional by constructing
 *
 * A {@link ReverseMapping} can be constructed for a given enumeration
 * <p>
 * The {@link IntEnumNativeTransformer} is used to marshal enumerations to/from the native layer.
 * <p>
 * @see EnumMask
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
	class ReverseMapping<E extends IntEnum> {
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

	/**
	 * Native transformer for integer enumerations.
	 */
	class IntEnumNativeTransformer implements NativeTransformer<IntEnum> {
		private final ReverseMapping<?> mapping;

		/**
		 * Constructor.
		 * @param type Enumeration type
		 */
		public IntEnumNativeTransformer(Class<? extends IntEnum> type) {
			this.mapping = new ReverseMapping<>(type);
		}

		@Override
		public MemoryLayout layout() {
			return ValueLayout.JAVA_INT;
		}

		@Override
		public Integer marshal(IntEnum e, SegmentAllocator allocator) {
			if(e == null) {
				return mapping.defaultValue().value();
			}
			else {
				return e.value();
			}
		}

		@Override
		public Function<Integer, IntEnum> unmarshal() {
			// TODO - getOrDefault()
			return value -> {
				if(value == 0) {
					return mapping.defaultValue();
				}
				else {
					return mapping.map(value);
				}
			};
		}
	}
}
