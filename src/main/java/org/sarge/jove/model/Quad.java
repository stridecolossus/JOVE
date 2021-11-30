package org.sarge.jove.model;

import static org.sarge.jove.common.Coordinate.Coordinate2D.BOTTOM_LEFT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.BOTTOM_RIGHT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.TOP_LEFT;
import static org.sarge.jove.common.Coordinate.Coordinate2D.TOP_RIGHT;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.sarge.jove.common.Coordinate.Coordinate2D;

/**
 * Quad utility class.
 * @author Sarge
 */
public final class Quad {
	/**
	 * Texture coordinates for a quad.
	 */
	public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);

	private Quad() {
	}

	/**
	 * Generates the indices for a quad strip.
	 * <p>
	 * If the <i>strip</i> argument is {@code true} this method generates a quad strip comprised of a {@link Primitive#TRIANGLE_STRIP}:
	 * <pre>
	 * int[] strip = Quad.strip(1, true);	// 012/123 234/345 etc
	 * </pre>
	 * If {@code false} the strip is comprised of {@link Primitive#TRIANGLES} with alternating triangle winding order:
	 * <pre>
	 * int[] strip = Quad.strip(1, true);	// 012/213 234/435 etc
	 * </pre>
	 * <p>
	 * @param quads 	Number of quads
	 * @param strip		Whether the quad is comprised of a {@link Primitive#TRIANGLE_STRIP} or alternating {@link Primitive#TRIANGLES}
	 * @return Triangle strip indices
	 */
	public static IntStream strip(int quads, boolean strip) {
		final IntFunction<? extends IntStream> right = strip ? Quad::triangle : Quad::clockwise;
		return IntStream.range(0, quads).flatMap(n -> IntStream.concat(triangle(n * 2), right.apply(n * 2 + 1)));
	}

	/**
	 * Generates the indices for a counter-clockwise triangle.
	 * @param index Starting index
	 * @return Counter-clockwise triangle indices
	 */
	public static IntStream triangle(int index) {
		return IntStream.rangeClosed(index, index + 2);
	}

	/**
	 * Generates the indices for a clockwise triangle.
	 * @param index Starting index
	 * @return Clockwise triangle indices
	 */
	public static IntStream clockwise(int index) {
		return IntStream.of(index + 1, index, index + 2);
	}
}
