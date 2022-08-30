package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.Function;

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
	public interface Intersects {
		/**
		 * Determines the intersections of this surface with the given ray.
		 * @param ray Ray
		 * @return Intersections
		 */
		Iterator<Intersection> intersections(Ray ray);

		/**
		 * Empty intersection(s).
		 */
		Iterator<Intersection> NONE = new Iterator<>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Intersection next() {
				throw new NoSuchElementException();
			}
		};
	}

	/**
	 * An <i>intersection</i> defines the position and normal at an intersection of this ray with a given surface.
	 */
	public class Intersection {
		private final Ray ray;
		private final float dist;
		private final Function<Point, Vector> normal;
		private Point pos;

		/**
		 * Constructor.
		 * @param ray			Intersected ray
		 * @param dist			Distance from origin
		 * @param normal		Normal function
		 */
		public Intersection(Ray ray, float dist, Function<Point, Vector> normal) {
			this.ray = notNull(ray);
			this.dist = zeroOrMore(dist);
			this.normal = notNull(normal);
		}

		/**
		 * Convenience constructor for an intersection with a literal normal.
		 * @param ray			Intersected ray
		 * @param dist			Distance from origin
		 * @param normal		Normal
		 */
		public Intersection(Ray ray, float dist, Vector normal) {
			this(ray, dist, ignored -> normal);
		}

		/**
		 * @return Intersection point
		 */
		public Point point() {
			if(pos == null) {
				pos = ray.point(dist);
			}
			return pos;
		}

		/**
		 * @return Surface normal at the given intersection
		 */
		public Vector normal() {
			return normal.apply(point());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Intersection that) &&
					(this.ray == that.ray) &&
					MathsUtil.isEqual(this.dist, that.dist) &&
					this.point().equals(that.point()) &&
					this.normal().equals(that.normal());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("dist", dist)
					.append("pos", point())
					.append("normal", normal())
					.build();
		}
	}
}
