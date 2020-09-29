package org.sarge.jove.geometry;

import org.sarge.jove.util.Check;

/**
 * Projection ray.
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
