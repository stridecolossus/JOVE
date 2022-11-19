package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Random;

import org.sarge.jove.geometry.Vector;

/**
 * Utility for generating randomised data.
 * <p>
 * This class wraps a {@link Random} instance to support unit-testing, since the Java random utility cannot easily be mocked.
 * <p>
 * @author Sarge
 */
public class Randomiser {
	private final Random random;
	private final Interpolator interpolator = Interpolator.linear(-1, +1);

	/**
	 * Constructor.
	 * @param random Randomiser
	 */
	public Randomiser(Random random) {
		this.random = notNull(random);
	}

	/**
	 * Default constructor.
	 */
	public Randomiser() {
		this(new Random());
	}

	/**
	 * @return Underlying randomiser
	 */
	public Random random() {
		return random;
	}

	/**
	 * Generates a random floating-point percentile value.
	 * @return Random value
	 * @see Random#nextFloat()
	 */
	public float next() {
		return random.nextFloat();
	}

	/**
	 * Generates a randomised vector.
	 * @return Randomised vector
	 */
	public Vector vector() {
		final float[] vec = new float[Vector.SIZE];
		for(int n = 0; n < vec.length; ++n) {
			vec[n] = interpolator.apply(next());
		}
		return new Vector(vec);
	}
}
