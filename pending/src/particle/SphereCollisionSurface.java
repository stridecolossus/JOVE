package org.sarge.jove.particle;

import org.sarge.jove.geometry.SphereVolume;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

public class SphereCollisionSurface implements CollisionSurface {
	private final SphereVolume sphere;

	public SphereCollisionSurface( SphereVolume sphere ) {
		Check.notNull( sphere );
		this.sphere = sphere;
	}

	@Override
	public boolean intersects( Particle p ) {
		return sphere.contains( p.getPosition() );
	}

	@Override
	public Vector reflect( Vector vec ) {
		return vec.invert();
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
