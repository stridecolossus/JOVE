package org.sarge.jove.common;

/**
 * 2D rectangle.
 * @author Sarge
 */
public record Rectangle(int x, int y, int width, int height) {
	/**
	 * Constructor.
	 */
	public Rectangle {
		// Without this ctor the IDE shows it as undefined (but the code compiles!) WTF
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
