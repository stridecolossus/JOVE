package org.sarge.jove.particle;

import java.util.Iterator;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>collision surface</i> is used to bound a particle system.
 * @author Sarge
 */
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
	 * Determines the intersection point(s) of the given particle ray.
	 * @param ray Particle ray
	 * @return Intersection(s)
	 */
	Iterator<Intersection> intersections(Ray ray);

	/**
	 * Creates a collision surface defined by a plane.
	 * Particles that are <i>behind</i> the plane (i.e. not {@link HalfSpace#POSITIVE) are considered as intersecting this surface.
	 * @param plane Plane
	 * @return Plane collision surface
	 * @see Plane#halfspace(Point)
	 */
	static CollisionSurface of(Plane plane) {
		return new CollisionSurface() {
			@Override
			public boolean intersects(Point pos) {
				return plane.halfspace(pos) != Plane.HalfSpace.POSITIVE;
			}

			@Override
			public Iterator<Intersection> intersections(Ray ray) {
				return plane.intersect(ray);
			}
		};
	}

	/**
	 * Creates a collision surface from a bounding volume.
	 * Particles that are <i>outside</i> the volume (according to {@link Volume#contains(Point)}) are considered as intersecting this surface.
	 * @param vol Bounding volume
	 * @return Bounding volume surface
	 * @see InverseVolume
	 */
	static CollisionSurface of(Volume vol) {
		return new CollisionSurface() {
			@Override
			public boolean intersects(Point pos) {
				return vol.contains(pos);
			}

			@Override
			public Iterator<Intersection> intersections(Ray ray) {
				return vol.intersect(ray);
			}
		};
	}
}
