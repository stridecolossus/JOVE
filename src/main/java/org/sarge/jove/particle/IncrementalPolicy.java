package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An <i>incremental policy</i> increases the number of particles up to a given maximum.
 * Note that fractional results are accumulated by this policy.
 * @author Sarge
 */
public class IncrementalPolicy implements GrowthPolicy {
	private final float inc;
	private final int max;

	private float pending;

	/**
	 * Constructor.
	 * @param inc Number of new particles to generate per second
	 * @param max Maximum number of particles
	 * @throws IllegalArgumentException if {@link #inc} is not positive or is larger than {@link max}
	 */
	public IncrementalPolicy(int inc, int max) {
		if(inc <= 0) throw new IllegalArgumentException("Increment must be positive");
		if(inc > max) throw new IllegalArgumentException("Increment cannot be larger than the maximum");
		this.inc = inc;
		this.max = oneOrMore(max);
	}

	@Override
	public int count(int current, float elapsed) {
		// Accumulate particles to generate and apply cap
		pending = Math.min(max - current, pending + inc * elapsed);

		// Determine actual number of particles to generate
		final int actual = (int) pending;
		pending -= actual;
		assert pending >= 0;

		return actual;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("inc", inc)
				.append("max", max)
				.build();
	}
}
