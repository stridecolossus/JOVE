package org.sarge.jove.scene.particle;

/**
 * A <i>generation policy</i> for a particle system specifies the number of particles to be generated per second.
 * @author Sarge
 */
public interface GenerationPolicy {
	/**
	 * Determines the number of particles to add on each frame.
	 * @param current 		Current number of particles
	 * @param elapsed		Elapsed modifier
	 * @return New particles to generate
	 */
	int count(int current, float elapsed);

	/**
	 * Policy for a particle system that does not generate new particles.
	 */
	GenerationPolicy NONE = (current, elapsed) -> 0;

	/**
	 * Creates a policy for a fixed number of particles.
	 * @param num Number of particles
	 * @return Fixed policy
	 */
	static GenerationPolicy fixed(int num) {
		return (current, __) -> num - current;
	}
}
