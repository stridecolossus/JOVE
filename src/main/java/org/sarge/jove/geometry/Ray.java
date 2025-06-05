package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>ray</i> is a vector relative to an originating point used for frustum culling, intersection tests, and picking.
 * @author Sarge
 */
public record Ray(Point origin, Vector direction) {
	/**
	 * Constructor.
	 * @param origin			Ray origin
	 * @param direction			Direction
	 */
	public Ray {
		requireNonNull(origin);
		requireNonNull(direction);
	}

	/**
	 * An <i>intersection</i> specifies the point(s) at which this ray intersects a {@link Surface}.
	 */
	public interface Intersection extends Comparable<Intersection> {
		/**
		 * An <i>intersection surface</i> can be tested for intersections with this ray.
		 */
		public interface Surface {
			/**
			 * Determines the intersections of the given ray with this surface.
			 * @param ray Ray
			 * @return Intersections
			 */
			Iterable<Intersection> intersections(Ray ray);
		}

		/**
		 * Empty intersections.
		 */
		Iterable<Intersection> NONE = List.of();

		/**
		 * Orders intersections by distance from the origin of this ray.
		 */
		Comparator<Intersection> COMPARATOR = Comparator.comparing(Intersection::distance);

		/**
		 * @return Distance of this intersection from the ray origin
		 */
		float distance();

		/**
		 * Calculates the intersection point on this ray, i.e. solves the line equation at this intersection.
		 * @return Intersection point
		 */
		Point point();

		/**
		 * Determines the surface normal at this intersection.
		 * @return Surface normal
		 */
		Normal normal();
	}

	/**
	 * Creates an intersection on this ray at the given distance with the given surface normal.
	 * @param distance		Intersection distance
	 * @param normal		Surface normal
	 * @return Intersection
	 */
	public Intersection intersection(float distance, Normal normal) {
		requireNonNull(normal);

		return new AbstractIntersection() {
			@Override
			public float distance() {
				return distance;
			}

			@Override
			public Normal normal() {
				return normal;
			}
		};
	}

	/**
	 * Creates an intersection on this ray at the given distance relative to the centre point of an intersected volume.
	 * @param distance		Intersection distance
	 * @param centre		Centre point
	 * @return Intersection
	 * @see AbstractIntersection#normal(Point)
	 */
	public Intersection intersection(float distance, Point centre) {
		requireNonNull(centre);

		return new AbstractIntersection() {
			@Override
			public float distance() {
				return distance;
			}

			@Override
			public Normal normal() {
				return normal(centre);
			}
		};
	}

	/**
	 * Skeleton implementation.
	 */
	public abstract class AbstractIntersection implements Intersection {
		@Override
		public Point point() {
			final Vector v = direction.multiply(this.distance());
			return origin.add(v);
		}

		/**
		 * Helper - Calculates the surface normal at this intersection relative to the given centre point of the intersected volume.
		 * @param centre Centre point
		 * @return Surface normal
		 */
		protected final Normal normal(Point centre) {
			final Vector v = Vector.between(centre, this.point());
			return new Normal(v);
		}

		@Override
		public int compareTo(Intersection that) {
			return Float.compare(this.distance(), that.distance());
		}

		@Override
		public int hashCode() {
			return Float.hashCode(this.distance());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					MathsUtility.isApproxEqual(this.distance(), that.distance()) &&
					this.normal().equals(that.normal());
		}

		@Override
		public String toString() {
			return String.format("Intersection[%s]", MathsUtility.format(this.distance()));
		}
	}
}
