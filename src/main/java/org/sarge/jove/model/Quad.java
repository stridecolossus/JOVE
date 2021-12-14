package org.sarge.jove.model;

import static org.sarge.jove.common.Coordinate.Coordinate2D.BOTTOM_LEFT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.BOTTOM_RIGHT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.TOP_LEFT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.TOP_RIGHT;

import java.util.List;
import java.util.stream.IntStream;

import org.sarge.jove.common.Coordinate.Coordinate2D;

/**
 * Quad utilities.
 * @author Sarge
 */
public final class Quad {
	/**
	 * Texture coordinates for a quad with a counter-clockwise winding order.
	 */
	public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);

	private Quad() {
	}

	/**
	 * Generates quad indices.
	 * @param clockwise Winding order
	 * @return Quad indices
	 */
	public static IntStream indices(boolean clockwise) {
		if(clockwise) {
			return IntStream.of(0, 2, 3, 1);
		}
		else {
			return IntStream.of(0, 1, 3, 2);
		}
	}

	/**
	 * Index factory for a quad strip.
	 */
	public static final IndexFactory STRIP = (count, clockwise) -> {
		return IntStream
				.range(0, count)
				.map(n -> n * 2)
				.flatMap(start -> IndexFactory.increment(start, indices(clockwise)));
	};
}
