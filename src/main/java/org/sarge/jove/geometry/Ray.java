package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

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
	 * Calculates the point on this ray at the given distance from the origin, i.e. solves the line equation.
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
		List<Intersection> intersections(Ray ray);

		/**
		 * Calculates the surface normal for the given intersection point.
		 * @param intersection Intersection point
		 * @return Surface normal
		 * @see #normal(Point, Point)
		 */
		Normal normal(Point intersection);

		/**
		 * Helper.
		 * Determines the surface normal at the given intersection point relative to the centre of this surface.
		 * @param intersection		Intersection point
		 * @param centre			Centre point of this surface
		 * @return Surface normal
		 */
		static Normal normal(Point intersection, Point centre) {
			return new Normal(Vector.between(centre, intersection));
		}

		/**
		 * Empty results for a ray that does not intersect this surface.
		 */
		List<Intersection> EMPTY_INTERSECTIONS = List.of();
	}

	/**
	 * An <i>intersection</i> specifies the distance along this ray where it intersects with a given surface.
	 */
	public record Intersection(float distance, IntersectedSurface surface) implements Comparable<Intersection> {
		/**
		 * Constructor.
		 * @param distance		Distance from the ray origin
		 * @param surface		Intersected surface
		 */
		public Intersection {
			requireZeroOrMore(distance);
			requireNonNull(surface);
		}

		@Override
		public int compareTo(Intersection that) {
			return Float.compare(this.distance, that.distance);
		}

		@Override
		public final boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					(this.surface == that.surface) &&
					MathsUtility.isApproxEqual(this.distance, that.distance);
		}
	}
}
