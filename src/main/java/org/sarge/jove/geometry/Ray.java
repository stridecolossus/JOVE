package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating point, used for intersection tests, picking, etc.
 * @author Sarge
 */
public interface Ray {
	/**
	 * @return Ray origin
	 */
	Point origin();

	/**
	 * Note that the ray direction is <b>not</b> constrained to be a {@link Normal}.
	 * @return Ray direction
	 */
	Vector direction();

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
    	 * <p>
    	 * The returned intersections are generally assumed to be ordered nearest to the surface.
    	 * <p>
		 * @param ray Ray
		 * @return Intersections
		 */
		Iterable<Intersection> intersections(Ray ray);

		/**
		 * Empty result.
		 */
		Iterable<Intersection> NONE = List.of();

		/**
		 * Intersection with undefined results, e.g. for use cases where the actual intersections (and normals) are not relevant.
		 */
		Iterable<Intersection> UNDEFINED = Arrays.asList((Intersection) null);
	}

	/**
	 * An <i>intersection</i> specifies where this ray intersects an {@link Intersected} surface.
	 */
	interface Intersection extends Comparable<Intersection> {
		/**
		 * @return Intersection distance from the ray origin
		 */
		float distance();

		/**
		 * Calculates the point on this ray at the given distance from the origin, i.e. solves the line equation for the intersection distance.
		 * @return Intersection point
		 */
		Point point();

		/**
		 * Surface normal at this intersection.
		 * @return Surface normal
		 * @throws UnsupportedOperationException if the normal is undefined
		 */
		default Normal normal() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Creates a simple intersection result.
		 * @param ray			Ray
		 * @param dist			Distance
		 * @param normal		Surface normal
		 * @return Intersection
		 */
		static Intersection of(Ray ray, float dist, Normal normal) {
			Check.notNull(normal);
			return new AbstractIntersection(ray, dist) {
				@Override
				public Normal normal() {
					return normal;
				}
			};
		}
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractIntersection implements Intersection {
		private final Ray ray;
		private final float dist;
		private Point pos;

		/**
		 * Constructor.
		 * @param ray 		Ray
		 * @param dist		Intersection distance
		 */
		public AbstractIntersection(Ray ray, float dist) {
			this.ray = notNull(ray);
			this.dist = dist;
		}

		@Override
		public float distance() {
			return dist;
		}

		@Override
		public final Point point() {
			if(pos == null) {
				final Point origin = ray.origin();
				final Vector dir = ray.direction().normalize();
				pos = origin.add(dir.multiply(distance()));
			}
			return pos;
		}

		@Override
		public final int compareTo(Intersection that) {
			return Float.compare(this.distance(), that.distance());
		}

		@Override
		public int hashCode() {
			return Objects.hash(ray, distance());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof AbstractIntersection that) &&
					this.ray.equals(that.ray) &&
					MathsUtil.isEqual(this.distance(), that.distance()) &&
					Objects.equals(this.normal(), that.normal());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("dist", distance())
					.append("normal", normal())
					.build();
		}
	}

	/**
	 * Default implementation that calculates the intersection normal relative to the surface centre point.
	 */
	class DefaultIntersection extends AbstractIntersection {
		private final Point centre;

		/**
		 * Constructor.
		 * @param ray			Ray
		 * @param dist			Intersection distance
		 * @param centre		Surface centre
		 */
		public DefaultIntersection(Ray ray, float dist, Point centre) {
			super(ray, dist);
			this.centre = notNull(centre);
		}

		@Override
		public Normal normal() {
			return Vector.between(centre, this.point()).normalize();
		}
	}
}
