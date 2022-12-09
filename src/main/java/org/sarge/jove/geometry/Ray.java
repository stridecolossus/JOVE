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
	 * @return Ray direction
	 */
	Normal direction();

	/**
	 * @return Length of this ray
	 */
	float length();

	/**
	 * Default implementation for an infinitely long ray.
	 */
	record DefaultRay(Point origin, Normal direction) implements Ray {
		/**
		 * Constructor.
		 * @param origin			Ray origin
		 * @param direction			Direction
		 */
		public DefaultRay {
			Check.notNull(origin);
			Check.notNull(direction);
		}

		@Override
		public float length() {
			return Float.POSITIVE_INFINITY;
		}
	}

	/**
	 * Defines a surface that can be tested for intersections with a ray.
	 */
	interface Intersected {
		/**
		 * Determines the intersections of this surface with the given ray.
		 * @param ray Ray
		 * @return Intersections
		 */
		Iterable<Intersection> intersections(Ray ray);

		/**
		 * Empty result.
		 */
		Iterable<Intersection> NONE = List.of();
	}

	/**
	 * An <i>intersection</i> specifies where this ray intersects an {@link Intersected} surface.
	 */
	interface Intersection {
		/**
		 * Orders intersections by distance from the ray origin.
		 */
		Comparator<Intersection> COMPARATOR = Comparator.comparing(Intersection::distance);

		/**
		 * @return Distance of this intersection from the ray origin
		 */
		float distance();

		/**
		 * Calculates the intersection point on the ray, i.e. solves the line equation for this {@link #distance()}.
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
				final Normal dir = ray.direction();
				pos = origin.add(dir.multiply(dist));
			}
			return pos;
		}

		@Override
		public int hashCode() {
			return Objects.hash(ray, dist);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					MathsUtil.isEqual(this.dist, that.distance()) &&
					Objects.equals(this.normal(), that.normal());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("dist", dist).build();
		}
	}

	/**
	 * Default implementation that calculates an intersection normal relative to the centre point of the surface.
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
