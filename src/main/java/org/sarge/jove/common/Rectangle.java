package org.sarge.jove.common;

/**
 * 2D rectangle with integer components.
 * @author Sarge
 */
public record Rectangle(int x, int y, int width, int height) {
	/**
	 * Constructor.
	 */
	public Rectangle(int width, int height) {
		this(0, 0, width, height);
	}

	/**
	 * Constructor for a rectangle at the origin.
	 * @param size Rectangle size
	 */
	public Rectangle(Dimensions size) {
		this(0, 0, size);
	}

	/**
	 * Constructor given rectangle dimensions.
	 */
	public Rectangle(int x, int y, Dimensions size) {
		this(x, y, size.width(), size.height());
	}

	/**
	 * @return Dimensions of this rectangle
	 */
	public Dimensions dimensions() {
		return new Dimensions(width, height);
	}
}
