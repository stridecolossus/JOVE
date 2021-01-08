package org.sarge.jove.model;

import static org.sarge.jove.geometry.Coordinate.Coordinate2D.BOTTOM_LEFT;
import static org.sarge.jove.geometry.Coordinate.Coordinate2D.BOTTOM_RIGHT;
import static org.sarge.jove.geometry.Coordinate.Coordinate2D.TOP_LEFT;
import static org.sarge.jove.geometry.Coordinate.Coordinate2D.TOP_RIGHT;

import java.util.List;

import org.sarge.jove.geometry.Coordinate.Coordinate2D;

/**
 * Quad utility class.
 * @author Sarge
 */
public final class Quad {
	/**
	 * Indices of the top-left triangle.
	 */
	public static final List<Integer> LEFT = List.of(0, 1, 2);

	/**
	 * Indices of the bottom-right triangle.
	 */
	public static final List<Integer> RIGHT = List.of(2, 1, 3);

	/**
	 * Texture coordinates for a quad.
	 */
	public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);

	private Quad() {
	}
}
