package org.sarge.jove.geometry;

import java.util.Arrays;
import java.util.List;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating position.
 * @author Sarge
 */
public record Ray(Point origin, Vector direction) {
	/**
	 * Constructor.
	 * @param origin		Ray origin
	 * @param direction		Direction vector
	 */
	public Ray {
		Check.notNull(origin);
		Check.notNull(direction);
	}

	/**
	 * Helper - Calculates the point on this ray at the given distance from the origin.
	 * @param dist Distance from the origin
	 * @return Point on this ray
	 */
	public Point point(float dist) {
		return origin.add(direction.scale(dist));
	}

	/**
	 * A <i>ray intersection</i> is a lazily evaluated list of intersection results on this ray.
	 */
	public static interface Intersection {
		/**
		 * @return List of intersection points expressed as distance(s) from the ray origin
		 * @see Ray#point(float)
		 */
		List<Float> distances();

		/**
		 * Empty intersection.
		 */
		Intersection NONE = Intersection.of();

		/**
		 * Helper - Creates an intersection result for a literal array of evaluated distance(s).
		 * @param intersections Intersection distance(s)
		 * @return Literal intersection
		 */
		static Intersection of(Float... dist) {
			final List<Float> list = Arrays.asList(dist);
			return () -> list;
		}
	}
}
