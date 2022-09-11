package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is specified as a vector relative to an originating position.
 * Rays are used for intersection tests, picking, etc.
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
			public float[] distances() {
				return new float[0];
			}

			@Override
			public boolean isEmpty() {
				return true;
			}
		};

		/**
		 * Intersection with undefined results.
		 */
		Intersection UNDEFINED = new Intersection() {
			@Override
			public float[] distances() {
				throw new UnsupportedOperationException();
			}
		};
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
	 * Point pt = ray.point(distances[0]);
	 *
	 * // Or arbitrarily select the intersection point nearest the surface
	 * Point pt = intersection.nearest(ray);
	 *
	 * // Calculate the surface normal at an intersection point
	 * Vector normal = intersection.normal(pt);
	 * <p>
	 * @see Intersected#NONE
	 * @see Intersected#UNDEFINED
	 */
	public interface Intersection {
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
		 * @throws UnsupportedOperationException if this intersection is undefined
		 */
		default Vector normal(Point p) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Calculates the <i>nearest</i> intersection point to the surface.
		 * @param ray Ray
		 * @throws UnsupportedOperationException if the intersection is {@link Intersected#UNDEFINED}
		 * @throws IllegalStateException if there are no intersections
		 * @see Ray#point(float)
		 */
		default Point nearest(Ray ray) {
			final float[] distances = distances();
			if(distances.length == 0) throw new IllegalStateException("No intersections: " + this);
			return ray.point(distances[0]);
		}

		/**
		 * Creates an intersection.
		 * @param d				Distance from ray origin
		 * @param normal		Surface normal
		 * @return New intersection
		 */
		static Intersection of(float d, Vector normal) {
			return new Intersection() {
				@Override
				public float[] distances() {
					return new float[]{d};
				}

				@Override
				public Vector normal(Point p) {
					return normal;
				}
			};
		}
	}
}
