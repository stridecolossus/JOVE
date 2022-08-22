package org.sarge.jove.particle;

import java.util.Random;

import org.sarge.jove.geometry.*;

/**
 * A <i>vector factory</i> generates the initial movement vector of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface VectorFactory {
	/**
	 * Generates the initial particle movement vector.
	 * @param pos Initial particle position
	 * @return Movement vector
	 */
	Vector vector(Point pos);

	/**
	 * Vector factory with the initial movement vector based on the starting position of the particle (relative to the origin).
	 * TODO - sphere
	 */
	VectorFactory POSITION = Vector::new;

	/**
	 * Creates a factory with a fixed initial vector.
	 * @param vec Movement vector
	 * @return Literal vector factory
	 */
	static VectorFactory of(Vector vec) {
		return ignored -> vec;
	}

	/**
	 * Creates a randomised vector factory.
	 */
	static VectorFactory random(Random random) {
		final float[] array = new float[3];
		return ignored -> {
			for(int n = 0; n < array.length; ++n) {
				array[n] = random.nextFloat();
			}
			return new Vector(array).normalize();
		};
	}
}
