package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Projection ray.
 * @author Sarge
 */
public final class Ray extends AbstractEqualsObject {
	private final Point origin;
	private final Vector dir;

	/**
	 * Constructor.
	 * @param origin		Ray origin
	 * @param dir			Direction vector
	 */
	public Ray(Point origin, Vector dir) {
		this.origin = notNull(origin);
		this.dir = notNull(dir);
	}

	/**
	 * @return Ray origin
	 */
	public Point origin() {
		return origin;
	}

	/**
	 * @return Ray direction
	 */
	public Vector direction() {
		return dir;
	}
}
