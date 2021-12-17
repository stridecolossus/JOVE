package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * An <i>index factory</i> generates indices for a strip of primitives.
 * <p>
 * In general the factory assumes that the vertices are arranged as a <i>quad</i> strip with alternating row indices.
 * <p>
 * For triangle based primitives each quad is assumed to consist of <b>two</b> triangles with counter-clockwise winding order as illustrated in the following example:
 * <pre>
 * 0 - 1 - 2
 * | / | / |
 * 3 - 4 - 5
 * </pre>
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

	/**
	 * Default index factory for a simple incremental index.
	 */
	IndexFactory DEFAULT = (index, count) -> IntStream.range(index, index + count);
}
