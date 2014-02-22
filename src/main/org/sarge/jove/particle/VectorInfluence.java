package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Adds a constant acceleration to a particle, e.g. to emulate gravity.
 * @author Sarge
 */
public class VectorInfluence implements Influence {
	private final Vector vec;

	/**
	 * Constructor.
	 * @param vec Acceleration vector
	 */
	public VectorInfluence( Vector vec ) {
		Check.notNull( vec );
		this.vec = vec;
	}

	@Override
	public void apply( Particle p, long elapsed ) {
		final float scale = elapsed / 1000f;
		p.add( vec.multiply( scale ) );
	}

	@Override
	public String toString() {
		return vec.toString();
	}
}
