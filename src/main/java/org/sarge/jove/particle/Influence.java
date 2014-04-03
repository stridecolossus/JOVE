package org.sarge.jove.particle;

/**
 * Influence on a particle.
 * @author Sarge
 */
public interface Influence {
	/**
	 * Applies this influence to the given particle.
	 * @param p			Particle
	 * @param elapsed	Time since last update (ms)
	 */
	void apply( Particle p, long elapsed );
}
