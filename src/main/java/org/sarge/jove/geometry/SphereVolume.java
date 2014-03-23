package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Sphere volume.
 * @author Sarge
 */
public class SphereVolume implements BoundingVolume {
	private final Point centre;
	private final float radius;

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public SphereVolume( Point centre, float radius ) {
		Check.notNull( centre );
		Check.zeroOrMore( radius );

		this.centre = new MutablePoint( centre );
		this.radius = radius;
	}

	/**
	 * Constructor for a sphere at the origin.
	 * @param radius Radius
	 */
	public SphereVolume( float radius ) {
		this( Point.ORIGIN, radius );
	}

	@Override
	public Point getCentre() {
		return centre;
	}

	/**
	 * @return Sphere radius
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public boolean contains( Point pos ) {
		return pos.distanceSquared( centre ) <= radius * radius;
	}

	private final MutableVector vec = new MutableVector();
	private final MutablePoint proj = new MutablePoint();

	// TODO - calc intersection point?

	@Override
	public boolean intersects( Ray ray ) {
		// Build vector from sphere to ray origin
		vec.subtract( centre, ray.getOrigin() );

		// Project sphere onto ray (unless behind ray)
		if( vec.dot( ray.getDirection() ) >= 0 ) {
			proj.project( ray.getDirection() );
			vec.subtract( centre, proj );
		}

		// Check distance to sphere centre
		return vec.getMagnitudeSquared() <= radius * radius;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
