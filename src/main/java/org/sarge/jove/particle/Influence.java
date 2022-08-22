package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;

/**
 * A <i>particle influence</i> is applied to a particle on <b>each</b> frame.
 * @author Sarge
 */
@FunctionalInterface
public interface Influence {
	/**
	 * Applies this influence to the given particle.
	 * @param particle Affected particle
	 */
	void apply(Particle particle);

	/**
	 * @return Whether this influence should be ignored by stopped particles
	 */
	default boolean ignoreStopped() {
		return true;
	}

	/**
	 * Creates a constant vector influence.
	 * @param vec Vector
	 * @return New vector influence
	 * @see Particle#add(Vector)
	 */
	static Influence of(Vector vec) {
		return p -> p.add(vec);
	}
}
