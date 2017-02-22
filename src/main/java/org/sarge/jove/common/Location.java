package org.sarge.jove.common;

import org.sarge.lib.util.EqualsBuilder;

/**
 * 2D integer coordinates.
 * @author Sarge
 */
public class Location {
	protected int x, y;

	/**
	 * Constructor.
	 * @param x
	 * @param y
	 */
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Copy constructor.
	 * @param loc
	 */
	public Location(Location loc) {
		this(loc.x, loc.y);
	}

	/**
	 * Origin constructor.
	 */
	public Location() {
		this(0, 0);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/**
	 * @param loc Location
	 * @return Distance-squared to the given location
	 */
	public float distanceSquared(Location loc) {
		final float dx = this.x - loc.x;
		final float dy = this.y - loc.y;
		return dx * dx + dy * dy;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.equals(this, obj);
	}

	@Override
	public String toString() {
		return x + "," + y;
	}
}
