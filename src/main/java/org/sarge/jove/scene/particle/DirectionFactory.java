package org.sarge.jove.scene.particle;

import org.sarge.jove.geometry.Normal;
import org.sarge.jove.util.Randomiser;

/**
 * A <i>direction factory</i> generates the initial movement direction of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface DirectionFactory {
	/**
	 * Generates the initial particle movement direction.
	 * @return Movement direction
	 */
	Normal direction();

	/**
	 * Creates a factory with a fixed initial direction.
	 * @param dir Movement direction
	 * @return Literal direction factory
	 */
	static DirectionFactory of(Normal dir) {
		return () -> dir;
	}

	/**
	 * Creates a direction factory as an adapter for the given randomiser.
	 * @param randomiser Vector randomiser
	 * @return Random direction factory
	 */
	static DirectionFactory random(Randomiser randomiser) {
		return () -> randomiser.vector().normalize();
	}

	/**
	 * Creates a cone direction factor defined by the given disc.
	 * @param disc Disc
	 * @return Cone direction factory
	 */
	static DirectionFactory cone(Disc disc) {
		return disc::vector;
	}
}
