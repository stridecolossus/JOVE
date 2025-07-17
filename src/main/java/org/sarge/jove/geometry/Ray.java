package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.List;

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
		 * Intersection results for a ray that does not intersect this surface.
		 */
		Iterable<Intersection> EMPTY_INTERSECTIONS = List.of();
	}

	/**
	 * An <i>intersection</i> specifies the point(s) at which this ray intersects an {@link IntersectedSurface}.
	 */
	public record Intersection(Point point, float distance, Normal normal) implements Comparable<Intersection> {
		@Override
		public int compareTo(Intersection that) {
			return Float.compare(this.distance(), that.distance());
		}
	}

	/**
	 * Creates an intersection at the given distance along this ray.
	 * @param distance		Intersection distance
	 * @param normal		Surface normal
	 * @return Intersection
	 */
	public Intersection intersection(float distance, Normal normal) {
		return new Intersection(point(distance), distance, normal);
	}

	/**
	 * Creates an intersection at the given distance along this ray with a normal to the centre of the intersected surface.
	 * @param distance		Intersection distance
	 * @param normal		Surface normal
	 * @return Intersection
	 */
	public Intersection intersection(float distance, Point centre) {
		final Point p = point(distance);
		final Vector n = Vector.between(centre, p);
		return new Intersection(p, distance, new Normal(n));
	}
}
