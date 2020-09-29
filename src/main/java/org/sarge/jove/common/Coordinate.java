package org.sarge.jove.common;

/**
 * Screen coordinates.
 * @author Sarge
 */
public record Coordinate(int x, int y) {
	/**
	 * Origin coordinate.
	 */
	public static final Coordinate ORIGIN = new Coordinate(0, 0);
}
