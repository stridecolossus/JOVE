package org.sarge.jove.model;

import java.util.stream.IntStream;

/**
 * Triangle utilities.
 * @author Sarge
 */
public final class Triangle {
	private Triangle() {
	}

	/**
	 * Index factory for a quad strip comprising a list of {@link Primitive#TRIANGLES}.
	 * <p>
	 * Each quad in the strip is comprised of two triangles with counter-clockwise winding order.
	 * <p>
	 * For example, given the following vertices:
	 * <pre>
	 * 0 1
	 * 2 3</pre>
	 * The indices of the generated triangles is {@code 021} and {@code 231}.
	 */
	public static final IndexFactory INDEX_TRIANGLES = (index, count) -> {
		final int next = index + count + 1;
		return IntStream.of(
				index, next, index + 1,
				next, next + 1, index + 1
		);
	};

	/**
	 * Index factory for a quad strip comprised of a {@link Primitive#TRIANGLE_STRIP}.
	 * <p>
	 * The generated index is comprised of the indices of the first triangle followed by alternating strip indices.
	 * <p>
	 * For example, given the following vertices:
	 * <pre>
	 * 0 1 2
	 * 3 4 5</pre>
	 * The indices of the generated strip is {@code 031425} etc.
	 * <p>
	 * Note that this implementation assumes that the index is used to render a triangle strip with alternating triangle winding order.
	 */
	public static final IndexFactory INDEX_STRIP = new IndexFactory() {
		@Override
		public IntStream indices(int index, int count) {
			return IntStream.of(index, index + count + 1);
		}

		@Override
		public IntStream strip(int start, int count) {
			return IntStream
					.rangeClosed(0, count)
					.flatMap(n -> indices(start + n, count));
		}
	};
	// TODO - degenerate triangle support
}
