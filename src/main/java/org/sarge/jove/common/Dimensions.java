package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * General 2D dimensions.
 * @author Sarge
 */
public record Dimensions(int width, int height) {
	/**
	 * Constructor.
	 * @param width 	Width
	 * @param height	Height
	 */
	public Dimensions {
		Check.zeroOrMore(width);
		Check.zeroOrMore(height);
	}

	/**
	 * @return Area of these dimensions
	 */
	public int area() {
		return width * height;
	}

	/**
	 * @return Ratio
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

	/**
	 * Tests whether these dimensions are larger than the given dimensions.
	 * @param dim Dimensions
	 * @return Whether larger than the given dimensions
	 */
	public boolean isLargerThan(Dimensions dim) {
		return (width > dim.width) || (height > dim.height);
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
