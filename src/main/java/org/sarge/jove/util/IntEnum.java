package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.util.*;
import java.util.function.Function;

import org.sarge.jove.foreign.Transformer;

/**
 * An <i>integer enumeration</i> represents a native {@code typedef enum} declaration.
 * <p>
 * The {@link #value()} method maps an enumeration constant to its underlying native value.
 * <p>
 * An integer enumeration can be treated as bidirectional using the {@link ReverseMapping} class.
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
	 * The <i>reverse mapping</i> is the inverse of this enumeration, i.e. maps a native integer <i>to</i> the corresponding enumeration constant.
	 * Note that duplicate values (i.e. synonyms) are silently ignored by this implementation.
	 * @param <E> Integer enumeration
	 * @see #defaultValue()
	 */
	class ReverseMapping<E extends IntEnum> {
		private final Map<Integer, E> map;
		private final E def;

		/**
		 * Constructor.
		 * @param type Integer enumeration type
		 */
		public ReverseMapping(Class<E> type) {
			final E[] array = type.getEnumConstants();
			this.map = Arrays.stream(array).collect(toMap(IntEnum::value, Function.identity(), (value, _) -> value));
			this.def = map.getOrDefault(0, array[0]);
		}

		/**
		 * The <i>default value</i> is the constant with a value of {@code zero} if present <b>or</b> arbitrarily the <b>first</b> entry in the enumeration.
		 * @return Default value for this enumeration
		 */
		public E defaultValue() {
			return def;
		}

		/**
		 * Maps the given native value to the corresponding enumeration constant.
		 * @param value Native value
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
	class IntEnumTransformer implements Transformer<IntEnum, Integer> {
		private final ReverseMapping<?> mapping;

		/**
		 * Constructor.
		 * @param type Enumeration type
		 */
		public IntEnumTransformer(Class<? extends IntEnum> type) {
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
		public IntEnum unmarshal(Integer value) {
			if(value == 0) {
				return mapping.defaultValue();
			}
			else {
				return mapping.map(value);
			}
		}
	}
}
