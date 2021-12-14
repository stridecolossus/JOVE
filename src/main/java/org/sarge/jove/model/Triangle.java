package org.sarge.jove.model;

import static org.sarge.jove.model.IndexFactory.increment;

import java.util.stream.IntStream;

/**
 * Triangle utilities.
 * @author Sarge
 */
public final class Triangle {
	private Triangle() {
	}

	/**
	 * Generates the indices for a triangle.
	 * <p>
	 * The indices are 012 for a triangle with a <i>counter-clockwise</i> winding order and 021 for a <i>clockwise</i> triangle.
	 * <p>
	 * @param index 		Starting index
	 * @param clockwise		Winding order
	 * @return Triangle indices
	 */
	public static IntStream indices(boolean clockwise) {
		if(clockwise) {
			return IntStream.of(0, 2, 1);
		}
		else {
			return IntStream.of(0, 1, 2);
		}
	}

	/**
	 * Index factory for a list of {@link Primitive#TRIANGLES}.
	 */
	public static final IndexFactory TRIANGLES = (count, clockwise) -> {
		return IntStream
				.range(0, count / 2)
				.map(n -> n * 2)
				.flatMap(start -> increment(start, alternating(clockwise)));
	};

	/**
	 * Helper - Generates indices for two triangles with alternate winding order.
	 * @param clockwise Winding order
	 * @return Alternating triangle indices
	 */
	private static IntStream alternating(boolean clockwise) {
		return IntStream.concat(
				indices(clockwise),
				increment(1, indices(!clockwise))
		);
	}

	/**
	 * Index factory for a {@link Primitive#TRIANGLE_STRIP}.
	 */
	public static final IndexFactory STRIP = (count, clockwise) -> {
		return IntStream
				.range(0, count)
				.flatMap(start -> increment(start, indices(clockwise)));
	};
}
