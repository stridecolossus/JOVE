package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * An <i>index factory</i> generates indices for a strip of primitives.
 * <p>
 * <p>
 * @author Sarge
 */
public interface IndexFactory {
	/**
	 * Generates indices for a primitives in this strip.
	 * @param index Primitive index
	 * @param count Number of primitives
	 * @return Indices
	 */
	IntStream indices(int index, int count);

	/**
	 * Generates indices for a strip.
	 * @param count Number of primitives
	 * @return Strip indices
	 */
	default IntStream strip(int count) {
		return IntStream
				.range(0, count)
				.flatMap(n -> indices(n, count));
	}
}
