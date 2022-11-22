package org.sarge.jove.scene.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.Randomiser;

/**
 * A <i>vector factory</i> generates the initial movement vector of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface VectorFactory {
	/**
	 * Generates the initial particle movement vector.
	 * @return Movement vector
	 */
	Vector vector();

	/**
	 * Creates a factory with a fixed initial vector.
	 * @param vec Movement vector
	 * @return Literal vector factory
	 */
	static VectorFactory of(Vector vec) {
		return () -> vec;
	}

	/**
	 * Creates a vector factory as an adapter for the given randomiser.
	 * @param randomiser Vector randomiser
	 * @return Random vector factory (normalised)
	 */
	static VectorFactory random(Randomiser randomiser) {
		return () -> randomiser.vector().normalize();
	}

	/**
	 * Creates a cone vector factor defined by the given disc.
	 * @param disc Disc
	 * @return Cone vector factory
	 */
	static VectorFactory cone(Disc disc) {
		return disc::vector;
	}
}
