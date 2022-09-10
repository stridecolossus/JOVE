package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.*;

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
	 * Creates a position factory that generates points on the surface of the given sphere.
	 * @param sphere		Sphere
	 * @param randomiser	Randomiser
	 * @return Spherical position factory
	 */
	static PositionFactory sphere(Sphere sphere, Randomiser randomiser) {
		return () -> {
			final Vector vec = randomiser.vector().normalize().multiply(sphere.radius());
			return sphere.centre().add(vec);
		};
	}

	/**
	 * Creates a position factory that generates points within the given box.
	 * @param box 			Box
	 * @param randomiser	Randomiser
	 * @return Box position factory
	 */
	static PositionFactory box(Bounds box, Randomiser randomiser) {
		final Point centre = box.centre();
		final Vector range = Vector.between(box.min(), box.max()).multiply(MathsUtil.HALF);
		return () -> {
			final Vector vec = randomiser.vector().multiply(range);
			return new Point(vec).add(centre);
		};
	}
}
