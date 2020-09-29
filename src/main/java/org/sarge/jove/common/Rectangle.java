package org.sarge.jove.common;

import org.sarge.jove.util.Check;

/**
 * 2D rectangle.
 * @author Sarge
 */
public record Rectangle(ScreenCoordinate pos, Dimensions size) {
	/**
	 * Constructor.
	 * @param pos Position
	 * @param dim Dimensions
	 */
	public Rectangle {
		Check.notNull(pos);
		Check.notNull(size);
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle(int x, int y, int width, int height) {
		this(new ScreenCoordinate(x, y), new Dimensions(width, height));
	}
}
