package org.sarge.jove.common;

import static org.sarge.lib.Validation.requireZeroOrMore;

/**
 * General 2D dimensions with integer components.
 * @author Sarge
 */
public record Dimensions(int width, int height) {
	/**
	 * Constructor.
	 * @param width 	Width
	 * @param height	Height
	 */
	public Dimensions {
		requireZeroOrMore(width);
		requireZeroOrMore(height);
	}

	/**
	 * @return Area of these dimensions
	 */
	public int area() {
		return width * height;
	}

	/**
	 * @return Aspect ratio
	 */
	public float ratio() {
		return width / (float) height;
	}

	/**
	 * @return Whether these dimensions are <i>square</i>
	 */
	public boolean isSquare() {
		return width == height;
	}

	public boolean contains(Dimensions that) {
		return (this.width >= that.width) && (this.height >= that.height);
	}

	/**
	 * @return These dimensions as a rectangle at the origin
	 */
	public Rectangle rectangle() {
		return new Rectangle(0, 0, this);
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
