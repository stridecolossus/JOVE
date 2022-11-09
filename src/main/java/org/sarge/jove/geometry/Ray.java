package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating point, used for intersection tests, picking, etc.
 * Note that the ray direction is <b>not</b> constrained to be a {@link Normal}.
 * @author Sarge
 */
public interface Ray {
	/**
	 * @return Ray origin
	 */
	Point origin();

	/**
	 * @return Ray direction
	 */
	Vector direction();

	/**
	 * Calculates the point on this ray at the given distance from the origin, i.e. solves the line equation for the given scalar.
	 * @param dist Distance from the origin
	 * @return Projected point on this ray
	 */
	default Point point(float dist) {
		final Point origin = this.origin();
		final Vector dir = this.direction().normalize();
		return origin.add(dir.multiply(dist));
	}

	/**
	 * Default implementation.
	 */
	record DefaultRay(Point origin, Vector direction) implements Ray {
		/**
		 * Constructor.
		 * @param origin			Ray origin
		 * @param direction			Direction
		 */
		public DefaultRay {
			Check.notNull(origin);
			Check.notNull(direction);
		}
	}

	/**
	 * Defines a surface that can be tested for intersections with a ray.
	 */
	interface Intersected {
		/**
		 * Determines the intersections of this surface with the given ray.
		 * @param ray Ray
		 * @return Intersection(s)
		 */
		Intersection intersection(Ray ray);
	}

	/**
	 * An <i>intersection</i> defines the points and normals where this ray intersects an {@link Intersected} surface.
	 * <p>
	 * The returned array of intersection {@link #distances()} are generally assumed to be ordered nearest to the surface.
	 * <p>
	 * The {@link #normal(Point)} method should be implemented for use-cases that require a surface normal.
	 * <p>
	 * Usage:
	 * <pre>
	 * // Get intersection(s)
	 * Intersection intersection = ...
	 *
	 * // Check for no intersections
	 * if(intersection.isEmpty()) { ... }
	 *
	 * // Determine nearest intersection point
	 * float[] distances = intersection.distances();
	 * Point p = ray.point(distances[0]);
	 *
	 * // Or arbitrarily select the nearest intersection to the surface
	 * Point nearest = intersection.nearest(ray);
	 *
	 * // Determine the surface normal at the intersection
	 * Normal normal = intersection.normal(p);
	 * <p>
	 */
	interface Intersection {
		/**
		 * Empty result for no intersections.
		 */
		Intersection NONE = new Intersection() {
			@Override
			public float[] distances() {
				return new float[0];
			}

			@Override
			public boolean isEmpty() {
				return true;
			}
		};

		/**
		 * Intersection with undefined results, e.g. for use cases where the actual intersections (and normals) are not relevant.
		 */
		Intersection UNDEFINED = new Intersection() {
			@Override
			public float[] distances() {
				throw new UnsupportedOperationException();
			}
		};

		/**
		 * @return Intersection distance(s)
		 * @throws UnsupportedOperationException if this intersection is undefined
		 */
		float[] distances();

		/**
		 * @return Whether any intersections are present
		 */
		default boolean isEmpty() {
			return false;
		}

		/**
		 * Determines the surface normal at the given intersection point on this ray.
		 * @param p Intersection point
		 * @return Surface normal
		 * @throws UnsupportedOperationException if this intersection is {@link #UNDEFINED}
		 */
		default Normal normal(Point p) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Calculates the <i>nearest</i> intersection point to the surface.
		 * @param ray Ray
		 * @throws IllegalStateException if there are no intersections
		 * @throws UnsupportedOperationException if this intersection is {@link #UNDEFINED}
		 * @see Ray#point(float)
		 */
		default Point nearest(Ray ray) {
			if(isEmpty()) throw new IllegalStateException("No intersections: " + this);
			final float[] distances = distances();
			return ray.point(distances[0]);
		}

		/**
		 * Helper - Creates a result for a single intersection.
		 * @param dist		Distance from ray origin
		 * @param normal	Surface normal
		 * @return New intersection
		 */
		static Intersection of(float dist, Normal normal) {
			return new Intersection() {
				@Override
				public float[] distances() {
					return new float[]{dist};
				}

				@Override
				public Normal normal(Point p) {
					return normal;
				}
			};
		}
	}
}
