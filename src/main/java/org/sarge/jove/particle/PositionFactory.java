package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;

/**
 * A <i>position factory</i> generates the starting position of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface PositionFactory {
	/**
	 * @return Particle position
	 */
	Point position();

	/**
	 * Origin factory.
	 */
	PositionFactory ORIGIN = of(Point.ORIGIN);

	/**
	 * Creates a position factory at the given point.
	 * @param pos Position
	 * @return Literal position factory
	 */
	static PositionFactory of(Point pos) {
		return () -> pos;
	}

	/**
	 * Creates a position factory that generates points on a sphere about the origin.
	 * @param radius 		Sphere radius
	 * @param randomiser	Randomiser
	 * @return Spherical position factory
	 */
	static PositionFactory sphere(SphereVolume sphere, Randomiser randomiser) {
		return () -> {
			final Vector vec = randomiser.vector().normalize().multiply(sphere.radius());
			return sphere.centre().add(vec);
		};
	}
	// TODO - should be centre/radius, no point in volume

	/**
	 * Creates a position factory that generates points within a box volume.
	 * @param bounds 		Box bounds
	 * @param randomiser	Randomiser
	 * @return Box position factory
	 */
	static PositionFactory box(Bounds bounds, Randomiser randomiser) {
		final Point min = bounds.min();
		final Vector range = Vector.between(min, bounds.max());
		return () -> {
			final Vector vec = randomiser.vector().multiply(range);
			return new Point(vec).add(min);
		};
	}
}
