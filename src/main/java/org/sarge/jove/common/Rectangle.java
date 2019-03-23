package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * 2D rectangle.
 * @author Sarge
 */
public final class Rectangle extends AbstractEqualsObject {
	private final ScreenCoordinate pos;
	private final Dimensions dim;

	/**
	 * Constructor.
	 * @param pos Position
	 * @param dim Dimensions
	 */
	public Rectangle(ScreenCoordinate pos, Dimensions dim) {
		this.pos = notNull(pos);
		this.dim = notNull(dim);
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

	/**
	 * @return Position
	 */
	public ScreenCoordinate position() {
		return pos;
	}

	/**
	 * @return Dimensions
	 */
	public Dimensions dimensions() {
		return dim;
	}

	@Override
	public String toString() {
		return pos + "(" + dim + ")";
	}
}
