package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>plane</i> is a flat surface in 3D space.
 * <p>
 * Mathematically the <i>general form</i> of a plane is:
 * <pre>ax + by + cz + d = 0</pre>
 * where <i>n</i> is the plane normal <code>n = (a, b, c)</code> and <i>d</i> is the distance of the plane from the origin.
 * <p>
 * Note that the distance increases in the <i>opposite</i> direction to the normal vector.
 * <br>
 * For example <code>new Plane(Vector.Y_AXIS), 1)</code> creates the plane in X-Z at Y = <b>minus</b> one.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Plane_(geometry)">Wikipedia</a>
 * @author Sarge
 */
public record Plane(Vector normal, float distance) {
	/**
	 * The half-space defines the <i>sides</i> of the plane where the plane normal points to the <i>positive</i> half-space.
	 */
	public enum HalfSpace {
		POSITIVE,
		NEGATIVE,
		INTERSECT;

		/**
		 * Determines the half-space of the given distance relative to this plane.
		 * @param d Distance to plane
		 * @return Half-space
		 */
		public static HalfSpace of(float d) {
			if(MathsUtil.isZero(d)) {
				return INTERSECT;
			}
			else
			if(d < 0) {
				return NEGATIVE;
			}
			else {
				return POSITIVE;
			}
		}
	}

	/**
	 * Creates a plane containing the given triangle of points.
	 * @param a
	 * @param b
	 * @param c
	 * @return New plane
	 */
	public static Plane of(Point a, Point b, Point c) {
		final Vector u = Vector.between(a, b);
		final Vector v = Vector.between(b, c);
		final Vector normal = u.cross(v).normalize();
		return of(normal, a);
	}

	/**
	 * Creates a plane given a normal and a point on the plane.
	 * @param normal		Plane normal
	 * @param pt			Point on the plane
	 * @return New plane
	 */
	public static Plane of(Vector normal, Point pt) {
		return new Plane(normal, -pt.dot(normal));
	}

	/**
	 * Constructor.
	 * @param normal		Plane normal
	 * @param dist			Distance of the plane from the origin
	 */
	public Plane {
		Check.notNull(normal);
	}

	/**
	 * Normalizes this plane.
	 * @return Normalized plane
	 * @see Vector#normalize()
	 */
	public Plane normalize() {
		final float len = MathsUtil.sqrt(normal.magnitude());
		if(MathsUtil.isEqual(len, 1)) {
			return this;
		}
		else {
			final float inv = 1 / len;
			return new Plane(normal.multiply(inv), distance * inv);
		}
	}

	/**
	 * Determines the distance of the given point from this plane.
	 * @param pt Point
	 * @return Distance to the given point
	 */
	public float distance(Point pt) {
		return normal.dot(pt) + distance;
	}

	/**
	 * Helper - Determines the half-space of the given point with respect to this plane.
	 * @param pt Point
	 * @return Half-space
	 * @see HalfSpace#of(float)
	 */
	public HalfSpace halfspace(Point pt) {
		return HalfSpace.of(distance(pt));
	}

	/**
	 * Calculates the intersection of the given ray with this plane.
	 * @param ray Ray
	 * @return Intersection
	 */
	public Intersection intersect(Ray ray) {
		// Calc denominator
		final float denom = normal.dot(ray.direction());

		// Stop if parallel
		if(MathsUtil.isZero(denom)) {
			return Intersection.NONE; // TODO - should this be zero?
		}

		// Calc intersection (note negative sign in equation)
		final float t = -distance(ray.origin()) / denom;
		if(t < 0) {
			return Intersection.NONE;
		}

		// Build intersection
		return Intersection.of(t);
	}
}
