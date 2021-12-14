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
	 */
	public static final IndexFactory TRIANGLES = new IndexFactory() {
		@Override
		public IntStream indices(int index, int count) {
			final int next = index + count + 1;
			return IntStream.of(
					index, next, index + 1,
					next, next + 1, index + 1
			);
		}
	};

	/**
	 * Index factory for a quad strip comprised of a {@link Primitive#TRIANGLE_STRIP}.
	 */
	public static final IndexFactory STRIP = new IndexFactory() {
		@Override
		public IntStream indices(int index, int count) {
			return IntStream.of(index, index + count + 1);
		}

		@Override
		public IntStream strip(int count) {
			return IntStream
					.rangeClosed(0, count)
					.flatMap(n -> indices(n, count));
		}
	};
}
