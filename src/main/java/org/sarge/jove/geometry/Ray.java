package org.sarge.jove.geometry;

import java.util.List;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is specified as a vector relative to an originating position, used for intersection tests, picking, etc.
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
		final Vector dir = this.direction();
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
	public interface Intersected {
		/**
		 * Determines the intersections of this surface with the given ray.
		 * @param ray Ray
		 * @return Intersection(s)
		 */
		Intersection intersection(Ray ray);

		/**
		 * Empty intersections.
		 */
		Intersection NONE = new Intersection() {
			@Override
			public List<Float> distances() {
				return List.of();
			}

			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public Vector normal(Point p) {
				throw new UnsupportedOperationException();
			}
		};

		/**
		 * Intersection with undefined results.
		 * @throws UnsupportedOperationException if the intersection point or normal is queried
		 */
		Intersection UNDEFINED = new Intersection() {
			@Override
			public List<Float> distances() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Vector normal(Point p) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * An <i>intersection</i> defines the points and normals where this ray intersects an {@link Intersected} surface.
	 * <p>
	 * Usage:
	 * <pre>
	 * // Get intersection(s)
	 * Intersection intersection = ...
	 *
	 * // Check for no intersections
	 * if(intersection.isEmpty()) { ... }
	 *
	 * // Determine an intersection point
	 * List distances = intersection.distances();
	 * float d = distances.get(0);
	 * Point pt = ray.point(d);
	 *
	 * // Or arbitrarily select the first intersection point
	 * Point pt = intersection.point(ray);
	 *
	 * // Calculate the surface normal at this intersection point
	 * Vector normal = intersection.normal(pt);
	 * <p>
	 * @see Intersected#NONE
	 */
	public interface Intersection {
		/**
		 * @return Intersection distance(s)
		 */
		List<Float> distances();

		/**
		 * @return Whether any intersections are present
		 */
		default boolean isEmpty() {
			return false;
		}

		/**
		 * Helper - Arbitrarily determines the <i>first</i> intersection point on the given ray.
		 * @param ray Ray
		 * @return Intersection point
		 * @throws IndexOutOfBoundsException if there are no intersections
		 * @see Ray#point(float)
		 */
		default Point point(Ray ray) {
			final float d = distances().get(0);
			return ray.point(d);
		}

		/**
		 * Determines the surface normal at the given intersection point on this ray.
		 * @param p Intersection point
		 * @return Surface normal
		 */
		Vector normal(Point p);

		/**
		 * Creates an intersection.
		 * @param d				Distance from ray origin
		 * @param normal		Surface normal
		 * @return New intersection
		 */
		static Intersection of(float d, Vector normal) {
			return new Intersection() {
				@Override
				public List<Float> distances() {
					return List.of(d);
				}

				@Override
				public Vector normal(Point p) {
					return normal;
				}
			};
		}
	}
}
