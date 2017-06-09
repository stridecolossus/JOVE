package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.ToString;

/**
 * Defines a surface for collisions with particles.
 * @author Sarge
 */
public interface CollisionSurface {
	/**
	 * @param pos Particle position
	 * @return Whether the given particle intersects this surface
	 * TODO - this should accept a vector representing the MOVEMENT of the particle, and use plane-line intersection (better reflection)
	 */
	boolean intersects(Particle p);

	/**
	 * Reflects the given vector from this surface.
	 * @param vec Vector to reflect
	 * @return Reflected vector
	 */
	Vector reflect(Vector vec);

	/**
	 * Creates the inverse of the given surface.
	 * @param surface Collision surface
	 * @return Inverted surface
	 */
	static CollisionSurface inverse(CollisionSurface surface) {
		return new CollisionSurface() {
			@Override
			public boolean intersects(Particle p) {
				return !surface.intersects(p);
			}

			@Override
			public Vector reflect(Vector vec) {
				return surface.reflect(vec);
			}

			@Override
			public String toString() {
				return ToString.toString(this);
			}
		};
	}
}
