package org.sarge.jove.geometry;

import java.util.Arrays;

import org.sarge.jove.util.MathsUtil;
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
	 * Helper - Calculates the point on this ray at the given distance from the origin, i.e. solves the line equation for the given scalar.
	 * @param dist Distance from the origin
	 * @return Point on this ray
	 */
	public Point point(float dist) {
		return origin.add(direction.multiply(dist));
	}

	/**
	 * A <i>ray intersection</i> is a lazily evaluated set of intersection results on this ray.
	 */
	public static interface Intersection {
		/**
		 * @return Intersection points expressed as distance from the ray origin
		 * @see Ray#point(float)
		 */
		float[] distances();

		/**
		 * Empty intersection.
		 */
		Intersection NONE = Intersection.of();

		/**
		 * Intersection consisting of distance zero.
		 */
		Intersection ZERO = () -> new float[]{0};

		/**
		 * Creates an intersection result for a literal array of intersections.
		 * @param intersections Intersection distances
		 * @return Literal intersection
		 */
		static Intersection of(float... dist) {
			return new Intersection() {
				@Override
				public float[] distances() {
					return dist;
				}

				@Override
				public boolean equals(Object obj) {
					return
							(obj == this) ||
							(obj instanceof Intersection that) &&
							MathsUtil.isEqual(this.distances(), that.distances());
				}

				@Override
				public String toString() {
					return Arrays.toString(dist);
				}
			};
		}
	}
}
