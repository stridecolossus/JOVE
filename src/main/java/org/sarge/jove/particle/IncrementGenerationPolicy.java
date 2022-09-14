package org.sarge.jove.particle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Element;

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

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("inc", inc).build();
	}

	/**
	 * Loads an incremental policy from the given element.
	 * @param e Element
	 * @return Incremental policy
	 */
	public static IncrementGenerationPolicy load(Element e) {
		final int inc = e.text().toInteger();
		return new IncrementGenerationPolicy(inc);
	}
}
