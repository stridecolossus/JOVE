package org.sarge.jove.particle;

import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Adapter for a collision surface defined by a plane.
 * <p>
 * A particle is defined as intersecting this surface when it is behind or on the plane.
 * The plane normal should therefore point towards the particle system.
 * <p>
 * @see Plane#getSide(Point)
 * @author Sarge
 */
public class PlaneCollisionSurface implements CollisionSurface {
	private final Plane plane;

	/**
	 * Constructor.
	 * @param plane Plane defining this surface
	 */
	public PlaneCollisionSurface( Plane plane ) {
		Check.notNull( plane );
		this.plane = plane;
	}

	@Override
	public boolean intersects( Particle p ) {
		final float dot = p.getDirection().dot( plane.getNormal() );
		if( dot > 0 ) return false;
		return plane.getSide( p.getPosition() ) != Plane.Side.FRONT;
	}

	@Override
	public Vector reflect( Vector vec ) {
		return vec.reflect( plane.getNormal() );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
