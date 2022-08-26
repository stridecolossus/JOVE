package org.sarge.jove.geometry;

import java.util.*;
import java.util.function.Supplier;

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
		 * @param direction			Direction
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
	 * A <i>ray intersection</i> is defined by a distance along this ray.
	 */
	public record Intersection(float distance, Vector normal) {
		/**
		 * Empty intersection(s).
		 */
		public static final Iterator<Intersection> NONE = new Iterator<>() {
			@Override
			public Intersection next() {
				throw new NoSuchElementException();
			}

			@Override
			public boolean hasNext() {
				return false;
			}
		};

		/**
		 * Constructor.
		 * @param distance		Intersection distance along this ray
		 * @param normal		Normal at this intersection point
		 */
		public Intersection {
			Check.notNull(normal);
		}

		@Override
		public boolean equals(Object obj) {
			return
				(obj == this) ||
				(obj instanceof Intersection that) &&
				MathsUtil.isEqual(this.distance, that.distance) &&
				this.normal.equals(that.normal);
		}

		/**
		 * Creates an iterator for a set of lazily-evaluated intersections.
		 * @param intersections Intersection(s) generator
		 */
		public static Iterator<Intersection> iterator(Supplier<List<Intersection>> intersections) {
			return new Iterator<>() {
				private Iterator<Intersection> itr;

				@Override
				public boolean hasNext() {
					if(itr == null) {
						itr = intersections.get().iterator();
					}
					return itr.hasNext();
				}

				@Override
				public Intersection next() {
					return itr.next();
				}
			};
		}
	}
}
