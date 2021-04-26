package org.sarge.jove.particle;

import java.util.Random;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vector factory</i> generates the initial movement vector of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface VectorFactory {
	/**
	 * Generates the initial particle movement vector.
	 * @param pos Initial particle position
	 * @return Movement vector
	 */
	Vector vector(Point pos);

	/**
	 * Vector factory with the initial movement vector based on the starting position of the particle (relative to the origin).
	 * This is generally intended for particles generated by a {@link SphericalPositionFactory}.
	 */
	VectorFactory POSITION = Point::toVector;

	/**
	 * Creates a randomised vector factory.
	 */
	static VectorFactory random(Random random) {
		return ignored -> {
			final float x = random.nextFloat();
			final float y = random.nextFloat();
			final float z = random.nextFloat();
			return new Vector(x, y, z).normalize();
		};
	}

	/**
	 * Creates a factory with a fixed initial vector.
	 * @param vec Movement vector
	 * @return Literal vector factory
	 */
	static VectorFactory literal(Vector vec) {
		return ignored -> vec;
	}

	/**
	 * Creates an adapter that scales a movement vector by the given velocity.
	 * @param factory		Underlying vector factory
	 * @param velocity		Particle velocity
	 * @return Scaled vector factory
	 */
	static VectorFactory scaled(VectorFactory factory, float velocity) {
		return pos -> factory.vector(pos).scale(velocity);
	}
}
