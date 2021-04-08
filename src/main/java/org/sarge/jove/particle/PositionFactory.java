package org.sarge.jove.particle;

import java.util.Random;

import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Sphere;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

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
		return () -> {
			final float phi = random.nextFloat() * MathsUtil.TWO_PI; // TODO - fiddle by 90 degrees?
			final float theta = random.nextFloat() * MathsUtil.PI - MathsUtil.HALF_PI;
			return Sphere.point(phi, theta, radius).toPoint();
		};

//		return () -> new Point(random.nextFloat(), random.nextFloat(), random.nextFloat()).scale(radius);
	}

	/**
	 * Creates a position factory that generates points within the given extents.
	 * @param extents 		Extents
	 * @param random		Random vector factory
	 * @return Position factory
	 */
	static PositionFactory extents(Extents extents, Random random) {
		return () -> {
			final Point min = extents.min();
			final Vector range = Vector.between(min, extents.max());
			return new Point(
					random(min.x(), range.x(), random),
					random(min.y(), range.y(), random),
					random(min.z(), range.z(), random)
			);
		};
	}

	private static float random(float min, float max, Random random) {
		return min + random.nextFloat() * (max - min);
	}
}
