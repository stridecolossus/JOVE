package org.sarge.jove.common;

import org.sarge.jove.util.Check;

/**
 * 2D rectangle.
 * @author Sarge
 */
public record Rectangle(Coordinate pos, Dimensions size) {
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
	 * Constructor for a rectangle at the {@link Coordinate#ORIGIN}.
	 * @param dim Rectangle dimensions
	 */
	public Rectangle(Dimensions dim) {
		this(Coordinate.ORIGIN, dim);
	}

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle(int x, int y, int width, int height) {
		this(new Coordinate(x, y), new Dimensions(width, height));
	}

	public int x() {
		return pos.x();
	}

	public int y() {
		return pos.y();
	}

	public int width() {
		return size.width();
	}

	public int height() {
		return size.height();
	}
}
