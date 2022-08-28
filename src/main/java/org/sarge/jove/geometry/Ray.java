package org.sarge.jove.geometry;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating position used for intersection tests, picking, etc.
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
	 * Default implementation.
	 */
	record DefaultRay(Point origin, Vector direction) implements Ray {
		/**
		 * Constructor.
		 * @param origin			Ray origin
		 * @param direction			Direction (assumes normalised)
		 */
		public DefaultRay {
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
	}

	/**
	 * Defines a surface that can be tested for intersections with a ray.
	 */
	interface Intersects {
		/**
		 * Determines the intersections of this surface with the given ray.
		 * @param ray Ray
		 * @return Intersections
		 */
		Iterator<Intersection> intersections(Ray ray);
	}

	/**
	 * A <i>ray intersection</i> is defined by a distance along the ray.
	 */
	public class Intersection {
		/**
		 * Empty intersection(s).
		 */
		public static final Iterator<Intersection> NONE = new Iterator<>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Intersection next() {
				throw new NoSuchElementException();
			}
		};

		/**
		 * Creates a static intersection result.
		 * @param dist			Intersection distance
		 * @param normal		Surface normal
		 * @return New intersection
		 */
		public static Intersection of(float dist, Vector normal) {
			Check.notNull(normal);

			return new Intersection(dist) {
				@Override
				public Vector normal() {
					return normal;
				}
			};
		}

		private final float dist;

		/**
		 * Constructor.
		 * @param dist Intersection distance
		 */
		public Intersection(float dist) {
			this.dist = dist;
		}

		/**
		 * @return Distance of this intersection along the ray
		 */
		public float distance() {
			return dist;
		}

		/**
		 * @return Surface normal at this intersection
		 * @throws UnsupportedOperationException by default
		 */
		public Vector normal() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return Objects.hash(dist, normal());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					MathsUtil.isEqual(this.distance(), that.distance()) &&
					this.normal().equals(that.normal());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(dist).append(normal()).build();
		}
	}
}
