package org.sarge.jove.scene.particle;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>collision</i> defines the result of a particle intersection.
 * @author Sarge
 */
@FunctionalInterface
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
	Collision DESTROY = (p, __) -> p.destroy();

	/**
	 * Stops a collided particle at the given intersection.
	 * @see Particle#stop(Point)
	 */
	Collision STOP = (p, intersection) -> {
		final Point pos = intersection.nearest(p);
		p.stop(pos);
	};
}
