package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;

/**
 * A <i>collision surface</i> is used to bound a particle system.
 * @author Sarge
 */
@FunctionalInterface
public interface CollisionSurface {
	/**
	 * Action on particles that intersect this surface.
	 */
	enum Action {
		/**
		 * Particle is destroyed.
		 */
		DESTROY,

		/**
		 * Particle stops, i.e. sticks to the surface.
		 * @see Particle#stop()
		 */
		STOP,

		/**
		 * Particle is reflected.
		 */
		REFLECT
	}

	/**
	 * Tests whether a particle intersects this surface.
	 * @param pos Particle position
	 * @return Whether intersects
	 */
	boolean intersects(Point pos);

	/**
	 * Creates a collision surface defined by a plane.
	 * Particles that are <i>behind</i> the plane (i.e. not {@link HalfSpace#POSITIVE) are considered as intersecting this surface.
	 * @param plane Plane
	 * @return Plane collision surface
	 * @see Plane#halfspace(Point)
	 */
	static CollisionSurface of(Plane plane) {
		return pos -> plane.halfspace(pos) != Plane.HalfSpace.POSITIVE;
	}

	/**
	 * Creates a collision surface from a bounding volume.
	 * Particles that are <i>outside</i> the volume (according to {@link Volume#contains(Point)}) are considered as intersecting this surface.
	 * @param vol Bounding volume
	 * @return Bounding volume surface
	 * @see InverseVolume
	 */
	static CollisionSurface of(Volume vol) {
		return vol::contains;
	}
}
