package org.sarge.jove.common;

/**
 * 2D rectangle.
 * @author Sarge
 */
public record Rectangle(int x, int y, int width, int height) {
	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle {
		// Empty
		// TODO - extra constructor seems to make canonical unavailable!?
	}

	/**
	 * Constructor for a rectangle at the origin.
	 * @param size Rectangle size
	 */
	public Rectangle(Dimensions size) {
		this(0, 0, size.width(), size.height());
	}

	/**
	 * @return Dimensions of this rectangle
	 */
	public Dimensions dimensions() {
		return new Dimensions(width, height);
	}
}
