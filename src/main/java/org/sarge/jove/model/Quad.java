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

	/**
	 * Index factory for quad strip with counter-clockwise winding order.
	 */
	public static final IndexFactory STRIP = (int n, int count) -> {
		final int next = n + count + 1;
		return IntStream.of(n, next, next + 1, n + 1);
	};

	private Quad() {
	}
}
