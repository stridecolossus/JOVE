package org.sarge.jove.util;

import static org.sarge.lib.util.Check.*;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.util.Converter;

/**
 * Converter for a floating-point tuple specified as a whitespace or comma-delimited string.
 * @param <T> Result type
 */
public class FloatArrayConverter<T> implements Converter<T> {
	private final int size;
	private final boolean exact;
	private final Function<float[], T> ctor;

	/**
	 * Constructor.
	 * @param size			Expected array length
	 * @param exact			Whether the array length <b>must</b> exactly match {@link #size}
	 * @param ctor			Constructor
	 */
	public FloatArrayConverter(int size, boolean exact, Function<float[], T> ctor) {
		this.size = oneOrMore(size);
		this.exact = exact;
		this.ctor = notNull(ctor);
	}

	/**
	 * Constructor.
	 * @param size			Expected array length
	 * @param ctor			Constructor
	 */
	public FloatArrayConverter(int size, Function<float[], T> ctor) {
		this(size, true, ctor);
	}

	@Override
	public T apply(String str) throws NumberFormatException {
		// Tokenize
		final String[] parts = StringUtils.split(str.trim(), " ,");
		if(!isValidLength(parts.length)) {
			throw new IllegalArgumentException("Expected tuple: actual=%d expected=%d exact=%b".formatted(parts.length, size, exact));
		}

		// Convert to array
		final float[] array = new float[parts.length];
		for(int n = 0; n < parts.length; ++n) {
			array[n] = Float.parseFloat(parts[n].trim());
		}

		// Create tuple
		return ctor.apply(array);
	}

	/**
	 * @return Whether the given array length is valid for this converter
	 */
	private boolean isValidLength(int len) {
		if(exact) {
			return len == size;
		}
		else {
			return len <= size;
		}
	}
}

