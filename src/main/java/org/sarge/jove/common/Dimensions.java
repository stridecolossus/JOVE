package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * General 2D dimensions.
 * @author Sarge
 */
public record Dimensions(int width, int height) implements Comparable<Dimensions> {
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

	@Override
	public int compareTo(Dimensions that) {
		if((this.width < that.width) || (this.height < that.height)) {
			return -1;
		}
		else
		if((this.width > that.width) || (this.height > that.height)) {
			return +1;
		}
		else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
