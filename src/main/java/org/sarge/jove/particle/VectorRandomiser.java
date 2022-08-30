package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import java.util.Random;

import org.sarge.jove.geometry.Vector;

/**
 * The <i>vector randomiser</i> is a utility for generating randomised vectors.
 * @author Sarge
 */
public class VectorRandomiser {
	private final Random random;
	private final float[] array = new float[3];

	/**
	 * Constructor.
	 * @param random Randomiser
	 */
	public VectorRandomiser(Random random) {
		this.random = notNull(random);
	}

	/**
	 * Constructor.
	 */
	public VectorRandomiser() {
		this(new Random());
	}

	/**
	 * Generates a randomised vector (not normalised).
	 * @return Randomised vector
	 */
	public Vector randomise() {
		for(int n = 0; n < array.length; ++n) {
			array[n] = random.nextFloat();
		}
		return new Vector(array);
	}
}
