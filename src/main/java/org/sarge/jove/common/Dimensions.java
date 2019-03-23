package org.sarge.jove.common;

import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Viewport or window dimensions.
 * @author Sarge
 */
public final class Dimensions extends AbstractEqualsObject {
	public final int width, height;

	/**
	 * Constructor.
	 * @param width 	Width
	 * @param height	Height
	 */
	public Dimensions(int width, int height) {
		this.width = zeroOrMore(width);
		this.height = zeroOrMore(height);
	}

	/**
	 * @return Width
	 */
	public int width() {
		return width;
	}

	/**
	 * @return Height
	 */
	public int height() {
		return height;
	}

	/**
	 * @return Ratio
	 */
	public float ratio() {
		return width / (float) height;
	}

	/**
	 * @return Whether these dimension are <i>square</i>
	 */
	public boolean isSquare() {
		return width == height;
	}

	/**
	 * @return Whether these dimensions are square and a power-of-two
	 * @see #isSquare()
	 * @see MathsUtil#isPowerOfTwo(int)
	 */
	public boolean isPowerOfTwo() {
		return isSquare() && MathsUtil.isPowerOfTwo(width);
	}

	/**
	 * Tests whether this dimensions is larger than the given dimensions.
	 * @param dim Dimensions
	 * @return Whether exceeded
	 */
	public boolean exceeds(Dimensions dim) {
		return (width > dim.width) || (height > dim.height);
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
