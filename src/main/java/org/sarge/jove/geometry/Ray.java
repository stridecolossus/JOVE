package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating position.
 * @author Sarge
 */
public record Ray(Point origin, Vector direction) {
	/**
	 * Constructor.
	 * @param origin		Ray origin
	 * @param direction		Direction vector
	 */
	public Ray {
		Check.notNull(origin);
		Check.notNull(direction);
	}
}
