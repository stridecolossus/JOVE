package org.sarge.jove.geometry;

import java.util.stream.Stream;

import org.sarge.lib.util.Check;

/**
 * A <i>ray</i> is a vector relative to an originating position.
 * @author Sarge
 */
public record Ray(Point origin, Vector direction) {
	/**
	 * Constructor.
	 * @param origin		Ray origin
	 * @param direction		Direction vector
	 */
	public Ray {
		Check.notNull(origin);
		Check.notNull(direction);
	}

	/**
	 * An <i>intersection</i> is a descriptor for the intersection of this ray with a volume.
	 * <p>
	 * The purpose of this interface is to allow the caller to query the number of intersections (if any) and apply appropriate application logic,
	 * while supporting lazily evaluation of the intersection point(s), i.e. an intersection is essentially a <i>supplier</i>.
	 * <p>
	 * Alternatively for cases where the intersection point is evaluated as a process or side-effect of the intersection logic the convenience {@link DefaultIntersection} can be returned.
	 * <p>
	 * The {@link #NONE} constant is used where the ray does not intersect with the volume.
	 */
	public static interface Intersection {
		/**
		 * @return Intersection point
		 */
		Point point();

		/**
		 * @return Minimum distance of this intersection to the ray (default implementation returns <b>zero</b>)
		 */
		float distance();

		/**
		 * Result for an empty list of intersections.
		 */
		Stream<Intersection> NONE = Stream.empty();

		/**
		 * Default implementation for an fully evaluated intersection.
		 */
		record DefaultIntersection(Point point, float distance) implements Intersection {
			/**
			 * Constructor.
			 * @param point				Intersection point
			 * @param distance			Minimum distance of this intersection to the ray
			 */
			public DefaultIntersection {
				Check.notNull(point);
			}
		}

		/**
		 * Helper - Creates a single intersection result.
		 * @param pt		Intersection point
		 * @param dist		Minimum distance
		 * @return Single intersection stream
		 */
		static Stream<Intersection> stream(Point pt, float dist) {
			return Stream.of(new DefaultIntersection(pt, dist));
		}
	}
}
