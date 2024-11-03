package org.sarge.jove.scene.particle;

/**
 * An <i>incremental generation policy</i> increases the number of particles with fractional results accumulated on each frame.
 * @author Sarge
 */
public class IncrementGenerationPolicy implements GenerationPolicy {
	private final float inc;
	private float pending;

	/**
	 * Constructor.
	 * @param inc Number of new particles to generate per second
	 * @param max Maximum number of particles
	 */
	public IncrementGenerationPolicy(float inc) {
		if(inc <= 0) throw new IllegalArgumentException("Increment must be positive");
		this.inc = inc;
	}

	@Override
	public int count(int current, float elapsed) {
		// Accumulate particles to generate
		pending += inc * elapsed;

		// Determine actual number of particles to generate
		final int actual = (int) pending;
		pending -= actual;
		assert pending >= 0;

		return actual;
	}
}
