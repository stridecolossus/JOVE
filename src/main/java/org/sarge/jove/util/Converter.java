package org.sarge.jove.util;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

/**
 * Converts a string to a data-type.
 * @author Sarge
 * @param <T> Data-type
 */
@FunctionalInterface
public interface Converter<T> extends Function<String, T> {
	/**
	 * Convert the given string to this data-type.
	 * @param str String to convert
	 * @return Converted data
	 * @throws NumberFormatException if the value cannot be converted
	 */
	@Override
	T apply(String str) throws NumberFormatException;

	/**
	 * Converts to an string (i.e. does nothing).
	 */
    Converter<String> STRING = str -> str;

	/**
	 * Converts to an integer.
	 */
	Converter<Integer> INTEGER = Integer::parseInt;

	/**
	 * Converts to floating-point.
	 */
	Converter<Float> FLOAT = Float::parseFloat;

	/**
	 * Converts to a long.
	 */
	Converter<Long> LONG = Long::parseLong;

	/**
	 * Converts to a boolean.
	 */
	Converter<Boolean> BOOLEAN = str -> {
		if(StringUtils.isEmpty(str)) {
			throw new NumberFormatException("Empty boolean");
		}
		else
		if(str.equalsIgnoreCase("true")) {
			return true;
		}
		else
		if(str.equalsIgnoreCase("false")) {
			return false;
		}
		else {
			throw new NumberFormatException("Invalid boolean: " + str);
		}
	};

	// TODO - following unused?

	/**
	 * Creates a converter for the given enumeration.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>comparisons are case-insensitive</li>
	 * <li>enumeration constants with under-score characters are replaced by hyphens</li>
	 * </ul>
	 * <p>
	 * @param clazz Enumeration class
	 * @return Enumeration converter
	 * @throws NumberFormatException if the constant is not valid
	 * @param <E> Enumeration
	 */
	static <E extends Enum<E>> Converter<E> enumeration(Class<E> clazz) throws NumberFormatException {
		return new EnumConverter<>(clazz);
	}

	class EnumConverter<E extends Enum<E>> implements Converter<E> {
		private final Map<String, E> map;

		public EnumConverter(Class<E> clazz) {
			final Function<E, String> mapper = e -> e.name().toLowerCase();
			map = Arrays.stream(clazz.getEnumConstants()).collect(toMap(mapper, Function.identity()));
		}

		@Override
		public E apply(String str) throws NumberFormatException {
			final E result = map.get(str.toLowerCase().replaceAll("-", "_"));
			if(result == null) throw new NumberFormatException("Unknown enum constant: " + str);
			return result;
		}
	}

	/**
	 * A <i>table converter</i> maps values from a lookup table or delegates to the underlying converter.
	 * @param <T> Type
	 */
	class TableConverter<T> implements Converter<T> {
		private final Map<String, T> table;
		private final Converter<T> converter;

		/**
		 * Constructor.
		 * @param converter		Delegate converter
		 * @param table 		Lookup table
		 */
		public TableConverter(Converter<T> converter, Map<String, T> table) {
			this.table = new HashMap<>(table);
			this.converter = notNull(converter);
		}

		/**
		 * Convenience constructor for a lookup table with a single entry.
		 * @param converter		Delegate converter
		 * @param key			Key
		 * @param value			Value
		 */
		public TableConverter(Converter<T> converter, String key, T value) {
			this(converter, Map.of(key, value));
		}

		/**
		 * Adds an entry to the lookup table.
		 * @param key		Key
		 * @param value		Value
		 * @return This converter
		 */
		public TableConverter<T> add(String key, T value) {
			table.put(key, value);
			return this;
		}

		@Override
		public T apply(String str) throws NumberFormatException {
			final T value = table.get(str);
			if(value == null) {
				return converter.apply(str);
			}
			else {
				return value;
			}
		}
	}
}
