package org.sarge.jove.particle;

import java.util.Random;

import org.sarge.jove.geometry.*;

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
	 * @param pt Point
	 * @return Point position factory
	 */
	static PositionFactory of(Point pt) {
		return () -> pt;
	}

	/**
	 * Creates a position factory that generates points on a sphere.
	 * @param radius 		Sphere radius
	 * @param random 		Random vector factory
	 * @return Spherical position factory
	 */
	static PositionFactory sphere(float radius, Random random) {
		// TODO - or use sphere point function?
		return () -> {
			final Vector vec = new Vector(random.nextFloat(), random.nextFloat(), random.nextFloat());
			return new Point(vec.normalize()).scale(radius);
		};
	}

	/**
	 * Creates a position factory that generates points within the given bounds.
	 * @param bounds 		Bounds
	 * @param random		Random vector factory
	 * @return Position factory
	 */
	static PositionFactory extents(Bounds bounds, Random random) {
		return () -> {
			final Point min = bounds.min();
			final Vector range = Vector.between(min, bounds.max());
			// TODO - use component/indices?
			return new Point(
					random(min.x, range.x, random),
					random(min.y, range.y, random),
					random(min.z, range.z, random)
			);
		};
	}

	private static float random(float min, float max, Random random) {
		return min + random.nextFloat() * (max - min);
	}
}
