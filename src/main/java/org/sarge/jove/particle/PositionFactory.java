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
	 * @param pos Position
	 * @return Literal position factory
	 */
	static PositionFactory of(Point pos) {
		return () -> pos;
	}

	/**
	 * Creates a position factory that generates points on a sphere about the origin.
	 * @param radius 		Sphere radius
	 * @param random		Randomiser
	 * @return Spherical position factory
	 */
	static PositionFactory sphere(SphereVolume sphere, Random random) {
		final float[] array = new float[3];
		return () -> {
			for(int n = 0; n < array.length; ++n) {
				array[n] = random.nextFloat();
			}
			final Vector vec = new Vector(array).normalize().multiply(sphere.radius());
			return sphere.centre().add(vec);
		};
	}

	/**
	 * Creates a position factory that generates points within a box volume.
	 * @param bounds 		Box bounds
	 * @param random		Randomiser
	 * @return Position factory
	 */
	static PositionFactory box(Bounds bounds, Random random) {
		final Point min = bounds.min();
		final Vector range = Vector.between(min, bounds.max());
		final float[] array = new float[3];
		return () -> {
			for(int n = 0; n < array.length; ++n) {
				array[n] = min.get(n) + random.nextFloat() * range.get(n);
			}
			return new Point(array);
		};
	}
}
