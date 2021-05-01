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
	 * Sides of a plane.
	 */
	public enum Side {
		FRONT,
		BACK,
		INTERSECT
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
		final float dist = -a.dot(normal);
		return new Plane(normal, dist);
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
	 * Determines the distance of the given point from this plane.
	 * @param pt Point
	 * @return Distance to the given point
	 */
	public float distance(Point pt) {
		return normal.dot(pt) + distance;
	}

	/**
	 * Determines which side of the plane the given point lies.
	 * @param pt Point
	 * @return Side
	 */
	public Side side(Point pt) {
		final float d = distance(pt);
		if(MathsUtil.isZero(d)) {
			return Side.INTERSECT;
		}
		else
		if(d < 0) {
			return Side.BACK;
		}
		else {
			return Side.FRONT;
		}
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

