package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.ToString;

/**
 * Velocity influence.
 * @author Sarge
 */
public class VelocityInfluence implements Influence {
	private final float speed;

	/**
	 * Constructor.
	 * @param speed Velocity scalar
	 */
	public VelocityInfluence( float speed ) {
		this.speed = speed;
	}

	@Override
	public void apply( Particle p, long elapsed ) {
		final Vector vec = p.getDirection();
		p.setDirection( vec.multiply( speed ) ); // TODO - mod by elapsed?
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
