package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * An <i>index factory</i> generates indices for a quad strip.
 * <p>
 * The vertices of the strip are assumed to column-major ordered:
 * <pre>
 * 0 - 1 - 2
 * | / | / |
 * 3 - 4 - 5
 * </pre>
 * @author Sarge
 */
@FunctionalInterface
public interface IndexFactory {
	/**
	 * Generates the indices for a quad strip.
	 * @param width Strip width
	 * @return Strip indices
	 */
	IntStream indices(int width);

	/**
	 * Creates an adapter that generates indices for the row of a grid.
	 * @param row Row index
	 * @return Row index factory
	 */
	default IndexFactory row(int row) {
		return width -> {
			final int start = row * width + 1;
			return indices(width).map(n -> n + start);
		};
	}

	/**
	 * Index factory for a strip comprising two triangles per quad.
	 * @see #triangles(int, int)
	 * @see Primitive#TRIANGLES
	 */
	IndexFactory TRIANGLES = width -> IntStream
			.range(0, width)
			.flatMap(n -> triangles(n, width));

	/**
	 * Index factory for a strip implemented as a triangle strip.
	 * @see Primitive#TRIANGLE_STRIP
	 */
	IndexFactory TRIANGLE_STRIP = width -> IntStream
			.rangeClosed(0, width)
			.flatMap(n -> IntStream.of(n, n + width + 1));

	/**
	 * Index factory for a quad strip.
	 */
	IndexFactory QUADS = width -> IntStream
			.range(0, width)
			.flatMap(n -> quad(n, width));

	/**
	 * Generates the indices for a quad comprising two triangles both with a counter-clockwise winding order.
	 * @param index		Quad index
	 * @param width		Strip width
	 * @return Quad indices
	 */
	static IntStream triangles(int index, int width) {
		final int next = index + 1;
		final int bottom = index + width + 1;
		return IntStream.of(
				index, bottom, next,
				bottom, bottom + 1, next
		);
	}

	/**
	 * Generates the indices for a quad with a counter-clockwise winding order.
	 * @param index Quad index
	 * @param width Strip width
	 * @return Quad indices
	 */
	static IntStream quad(int index, int width) {
		final int next = index + width + 1;
		return IntStream.of(index, next, next + 1, index + 1);
	}
}
