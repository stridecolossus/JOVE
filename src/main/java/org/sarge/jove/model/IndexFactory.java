package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * An <i>index factory</i> generates indices for a strip of primitives.
 * <p>
 * <p>
 * @author Sarge
 */
@FunctionalInterface
public interface IndexFactory {
	/**
	 * Generates indices for a strip of primitives.
	 * @param count Number of primitives
	 * @return Indices
	 */
	IntStream index(int count, boolean clockwise);

	/**
	 * Helper - Increments a stream of indices.
	 * @param inc			Increment
	 * @param stream		Stream
	 * @return Incremented stream
	 */
	static IntStream increment(int inc, IntStream stream) {
		return stream.map(n -> n + inc);
	}
}
