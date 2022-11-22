package org.sarge.jove.scene.particle;

import org.sarge.jove.common.Colour;

/**
 * A <i>colour factory</i> determines the colour of a particle.
 * @author Sarge
 */
@FunctionalInterface
public interface ColourFactory {
	/**
	 * Determines the colour of a particle.
	 * @param t Elapsed scalar
	 * @return Particle colour
	 */
	Colour colour(float t);

	/**
	 * @return Whether the particle colour is modified on each frame
	 */
	default boolean isModified() {
		return true;
	}

	/**
	 * Creates a literal colour factory for a fixed colour.
	 * @param col Colour
	 * @return New literal colour factory
	 */
	static ColourFactory of(Colour col) {
		return new ColourFactory() {
			@Override
			public Colour colour(float __) {
				return col;
			}

			@Override
			public boolean isModified() {
				return false;
			}
		};
	}
}
