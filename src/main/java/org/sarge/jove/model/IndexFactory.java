package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * An <i>index factory</i> generates indices for a strip of primitives.
 * <p>
 * In general the factory should assume that vertices are arranged as a grid of quads with incremental indices.
 * <p>
 * For example, for a 3-by-3 grid the vertices are:
 * <pre>
 * 0 1 2
 * 3 4 5
 * 6 7 8</pre>
 * @author Sarge
 */
public interface IndexFactory {
	/**
	 * Generates indices for a primitives in this strip.
	 * @param index Primitive index
	 * @param count Number of quads in a strip
	 * @return Indices
	 */
	IntStream indices(int index, int count);

	/**
	 * Generates indices for a strip.
	 * @param count Number of quads in this strip
	 * @return Strip indices
	 */
	default IntStream strip(int count) {
		return IntStream
				.range(0, count)
				.flatMap(n -> indices(n, count));
	}
}
