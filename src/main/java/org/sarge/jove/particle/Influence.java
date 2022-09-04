package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;

/**
 * A <i>particle influence</i> modifies the state of an active particle.
 * @author Sarge
 */
public interface Influence {
	/**
	 * Applies this influence to the given particle.
	 * @param p 			Particle to influence
	 * @param elapsed		Elapsed scalar
	 */
	void apply(Particle p, float elapsed);

	/**
	 * Creates an acceleration influence by incrementing the particle direction by the given vector.
	 * @param vec Acceleration vector
	 * @return New acceleration influence
	 * @see Particle#add(Vector)
	 */
	static Influence of(Vector vec) {
		return (p, elapsed) -> p.add(vec.multiply(elapsed));
	}

	/**
	 * Creates an acceleration influence by applying the given velocity to a particle.
	 * @param v Velocity modifier
	 * @return New acceleration influence
	 * @see Particle#velocity(float)
	 */
	static Influence velocity(float v) {
		return (p, elapsed) -> p.velocity(v * elapsed);
	}
}
