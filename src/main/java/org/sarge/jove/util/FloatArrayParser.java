package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.function.Function;

/**
 * A <i>float array parser</i> converts a comma-delimited string to a floating-point type.
 * @param <T> Result type
 */
public class FloatArrayParser<T> implements Function<String, T> {
	private final int size;
	private final Function<float[], T> ctor;

	/**
	 * Constructor.
	 * @param size			Expected array length
	 * @param ctor			Constructor
	 */
	public FloatArrayParser(int size, Function<float[], T> ctor) {
		this.size = requireOneOrMore(size);
		this.ctor = requireNonNull(ctor);
	}

	@Override
	public T apply(String str) throws NumberFormatException {
		// Tokenize
		// TODO - delimiter (?)
		final String[] parts;
		if(str.indexOf(',') > 0) {
			parts = str.trim().split(",");
		}
		else {
			parts = str.trim().split(" ");
		}
		if(parts.length != size) {
			throw new IllegalArgumentException("Invalid tuple length: actual=%d expected=%d".formatted(parts.length, size));
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
