package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * A ray defined by an origin and direction, used for scene picking.
 * @author Sarge
 * @see BoundingVolume
 */
public class Ray {
	private final Point origin;
	private final Vector dir;

	/**
	 * Constructor.
	 * @param origin	Ray origin
	 * @param dir		Ray direction vector
	 */
	public Ray( Point origin, Vector dir ) {
		Check.notNull( origin );
		Check.notNull( dir );

		this.origin = new MutablePoint( origin );
		this.dir = new MutableVector( dir ).normalize();
	}

	/**
	 * @return Origin of this ray
	 */
	public Point getOrigin() {
		return origin;
	}

	/**
	 * @return Direction vector
	 */
	public Vector getDirection() {
		return dir;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
