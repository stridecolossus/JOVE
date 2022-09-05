package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>collision</i> defines the result of a particle intersection.
 * @author Sarge
 */
public interface Collision {
	/**
	 * Applies this collision to the given particle.
	 * @param p					Particle
	 * @param intersection		Intersection(s)
	 */
	void collide(Particle p, Intersection intersection);

	/**
	 * Destroys a collided particle.
	 * @see Particle#destroy()
	 */
	Collision DESTROY = (p, ignored) -> p.destroy();

	/**
	 * Stops a collided particle at the given intersection.
	 * @see Particle#stop(Point)
	 */
	Collision STOP = (p, intersection) -> p.stop(intersection.point(p));

	/**
	 * Reflects a collided particle about the surface normal at the intersection.
	 * @see Particle#reflect(Point, Vector)
	 */
	Collision REFLECT = (p, intersection) -> {
		final Point pt = intersection.point(p);
		final Vector normal = intersection.normal(pt);
		p.reflect(pt, normal);
	};
}
