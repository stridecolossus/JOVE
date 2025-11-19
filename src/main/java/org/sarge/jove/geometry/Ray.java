package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.List;

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
	 * Calculates the point on this ray at the given distance, i.e. solves the line equation.
	 * @param distance Distance along this ray
	 * @return Point on ray
	 */
	public Point point(float distance) {
		final Vector v = direction.multiply(distance);
		return origin.add(v);
	}

	/**
	 * An <i>intersected surface</i> defines a volume that can be tested for intersections by this ray.
	 */
	public interface IntersectedSurface {
		/**
		 * Enumerates the intersections of the given ray with this surface.
		 * @param ray Ray
		 * @return Intersections
		 */
		Iterable<Intersection> intersections(Ray ray);

		/**
		 * Empty results for a ray that does not intersect this surface.
		 */
		Iterable<Intersection> EMPTY_INTERSECTIONS = List.of();
	}

	/**
	 * An <i>intersection</i> specifies the point(s) where this ray intersects a {@link IntersectedSurface}.
	 */
	public interface Intersection extends Comparable<Intersection> {
		/**
		 * @return Distance from the ray origin
		 */
		float distance();

		/**
		 * @return Intersection point
		 * @see Ray#point(float)
		 */
		Point point();

		/**
		 * Surface normal at this intersection.
		 */
		Normal normal();
	}

	/**
	 * Skeleton implementation.
	 */
	public abstract class AbstractIntersection implements Intersection {
		private final float distance;

		/**
		 * Constructor.
		 * @param distance Intersection distance
		 */
		protected AbstractIntersection(float distance) {
			this.distance = distance;
		}

		@Override
		public float distance() {
			return distance;
		}

		@Override
		public Point point() {
			return Ray.this.point(distance);
		}

		@Override
		public int compareTo(Intersection that) {
			return Float.compare(this.distance(), that.distance());
		}

		@Override
		public int hashCode() {
			return Float.hashCode(distance);
		}

		@Override
		public final boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					MathsUtility.isApproxEqual(distance, that.distance());
		}

		@Override
		public String toString() {
			return String.format("Intersection[dist=%d]", distance);
		}
	}

	/**
	 * Helper.
	 * Creates an intersection at the given distance along this ray.
	 * @param distance		Intersection distance
	 * @param normal		Surface normal
	 * @return Intersection
	 * @see #point(float)
	 */
	public Intersection intersection(float distance, Normal normal) {
		requireNonNull(normal);
		return new AbstractIntersection(distance) {
			@Override
			public Normal normal() {
				return normal;
			}
		};
	}

	/**
	 * Helper.
	 * Creates an intersection at the given distance along this ray that calculates the surface normal to the centre of the intersected surface.
	 * @param distance		Intersection distance
	 * @param centre		Centre point of the intersected surface
	 * @return Intersection
	 */
	public Intersection intersection(float distance, Point centre) {
		requireNonNull(centre);
		return new AbstractIntersection(distance) {
			@Override
			public Normal normal() {
				return new Normal(Vector.between(centre, this.point()));
			}
		};
	}
}
