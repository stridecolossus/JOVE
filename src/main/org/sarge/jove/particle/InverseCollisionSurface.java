package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Inverse collision surface.
 * @author Sarge
 */
public class InverseCollisionSurface implements CollisionSurface {
	private final CollisionSurface surface;

	/**
	 * Constructor.
	 * @param surface Delegate surface
	 */
	public InverseCollisionSurface( CollisionSurface surface ) {
		Check.notNull( surface );
		this.surface = surface;
	}

	@Override
	public boolean intersects( Particle p ) {
		return !surface.intersects( p );
	}

	@Override
	public Vector reflect( Vector vec ) {
		return surface.reflect( vec );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
