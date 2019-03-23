package org.sarge.jove.common;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Screen coordinates.
 * @author Sarge
 */
public final class ScreenCoordinate extends AbstractEqualsObject {
	public final int x, y;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 */
	public ScreenCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}
