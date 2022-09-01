package org.sarge.jove.util;

import java.util.Random;

/**
 * A <i>float supplier</i> is a convenience generator for a floating-point value.
 * @author Sarge
 */
@FunctionalInterface
public interface FloatSupplier {
    /**
     * @return Floating-point value
     */
	float get();

	/**
	 * Creates a float supplier that generates random 0..1 percentile values.
	 * @param random Randomiser
	 * @return Random float supplier
	 */
	static FloatSupplier of(Random random) {
		return () -> random.nextFloat();
	}
}
