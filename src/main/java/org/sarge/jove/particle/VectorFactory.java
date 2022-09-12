package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

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
		return pos -> vec;
	}

	/**
	 * Creates a vector factory as an adapter for the given randomiser.
	 * @param randomiser Vector randomiser
	 * @return Random vector factory (normalised)
	 */
	static VectorFactory random(Randomiser randomiser) {
		return pos -> randomiser.vector().normalize();
	}
}
