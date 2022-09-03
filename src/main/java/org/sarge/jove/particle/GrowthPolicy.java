package org.sarge.jove.particle;

/**
 * A <i>growth policy</i> for a particle system specifies the number of particles to be generated per second.
 * @author Sarge
 */
public interface GrowthPolicy {
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
	GrowthPolicy NONE = (current, elapsed) -> 0;

	/**
	 * Creates a policy that increments the number of particles.
	 * @param inc Number of particles to generate
	 * @return Incremental policy
	 */
	static GrowthPolicy increment(int inc) {
		return (current, elaped) -> inc;
	}

	/**
	 * Creates a policy for a fixed number of particles.
	 * @param num Number of particles
	 * @return Fixed policy
	 */
	static GrowthPolicy fixed(int num) {
		return (current, elapsed) -> num - current;
	}
}
