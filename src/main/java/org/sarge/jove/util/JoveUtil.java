package org.sarge.jove.util;

import java.util.function.Function;

/**
 * JOVE utility methods.
 * @author Sarge
 */
public final class JoveUtil {
	private JoveUtil() {
		// Utilities class
	}

	/**
	 * Creates a converter for a comma-delimited string of floating-point values.
	 * @param len		Expected number of components
	 * @param ctor 		Array constructor
	 * @param <T> Result data-type
	 * @return Converter
	 */
	public static <T> Converter<T> converter(int len, Function<float[], T> ctor) {
		return str -> {
			// Tokenize tuple
			final String[] parts = str.trim().split(",");
			if(parts.length != len) throw new NumberFormatException("Invalid number of tuple components: " + str);

			// Convert to array
			final float[] array = new float[len];
			for(int n = 0; n < array.length; ++n) {
				array[n] = Float.parseFloat(parts[n].trim());
			}

			// Create tuple
			return ctor.apply(array);
		};
	}
}
