package org.sarge.jove.particle;

import java.util.function.Supplier;

import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

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
	static PositionFactory sphere(float radius, Supplier<Vector> random) {
		return () -> new Point(random.get().scale(radius));
	}

	/**
	 * Creates a position factory that generates points within the given extents.
	 * @param extents 		Extents
	 * @param random		Random vector factory
	 * @return Position factory
	 */
	static PositionFactory extents(Extents extents, Supplier<Vector> random) {
		final Point min = extents.min();
		final Vector range = Vector.of(extents.min(), extents.max());
		return () -> {
			final Vector v = random.get();
			final Vector vec = new Vector(v.x * range.x, v.y * range.y, v.z * range.z);
			return min.add(vec);
		};
	}
}
