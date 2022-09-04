package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Random;

import org.sarge.jove.geometry.Vector;

/**
 * Utility for generating randomised data.
 * <p>
 * This class wraps a {@link Random} instance to enable easier unit-testing, since the Java random utility cannot easily be mocked.
 * <p>
 * @author Sarge
 */
public class Randomiser {
	private final Random random;
	private final float[] array = new float[3];

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
	 * This method is <b>not</b> thread-safe.
	 * @return Randomised vector
	 */
	public Vector vector() {
		for(int n = 0; n < array.length; ++n) {
			array[n] = Interpolator.lerp(-1, +1, next());
		}
		return new Vector(array);
	}
}
