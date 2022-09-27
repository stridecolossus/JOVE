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
	private final Function<float[], T> ctor;

	/**
	 * Constructor.
	 * @param size			Expected array length
	 * @param ctor			Constructor
	 */
	public FloatArrayConverter(int size, Function<float[], T> ctor) {
		this.size = oneOrMore(size);
		this.ctor = notNull(ctor);
	}

	@Override
	public T apply(String str) throws NumberFormatException {
		// Tokenize
		final String[] parts = StringUtils.split(str.trim(), " ,");
		if(parts.length != size) {
			throw new IllegalArgumentException("Expected tuple: actual=%d expected=%d".formatted(parts.length, size));
		}

		// Convert to array
		final float[] array = new float[parts.length];
		for(int n = 0; n < parts.length; ++n) {
			array[n] = Float.parseFloat(parts[n].trim());
		}

		// Create tuple
		return ctor.apply(array);
	}
}
