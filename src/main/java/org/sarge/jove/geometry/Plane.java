package org.sarge.jove.geometry;

import java.util.*;

import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>plane</i> defines a flat surface in 3D space.
 * <p>
 * Mathematically the <i>general form</i> of a plane is:
 * <pre>ax + by + cz + d = 0</pre>
 * where <code>n = (a, b, c)</code> is the plane normal and <i>d</i> is the distance of the plane from the origin.
 * <p>
 * Note that the distance increases in the <i>opposite</i> direction to the normal vector.
 * <br>
 * For example <code>new Plane(Vector.Y_AXIS), 1)</code> creates the plane in X-Z at Y = <b>minus</b> one.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Plane_(geometry)">Wikipedia</a>
 * @author Sarge
 */
public record Plane(Vector normal, float distance) implements Intersected {
	/**
	 * The half-space defines the <i>sides</i> of this plane with respect to the normal.
	 * The {@link #POSITIVE} half-space is in <i>front</i> of the plane and {@link #NEGATIVE} is <i>behind</i>.
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
			if(d < 0) {
				return NEGATIVE;
			}
			else
			if(d > 0) {
				return POSITIVE;
			}
			else {
				return INTERSECT;
			}
		}
	}

	/**
	 * Creates a plane containing the given triangle of points.
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
	 * @param n Plane normal
	 * @param p Point on the plane
	 * @return New plane
	 */
	public static Plane of(Vector n, Point p) {
		return new Plane(n, -p.dot(n));
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
	 */
	public Plane normalize() {
		final float len = normal.magnitude();
		if(MathsUtil.isEqual(len, 1)) {
			return this;
		}
		else {
			final float inv = MathsUtil.inverseRoot(len);
			return new Plane(normal.multiply(inv), distance * inv);
		}
	}

	/**
	 * Determines the distance of the given point from this plane.
	 * @param p Point
	 * @return Distance to the given point
	 */
	public float distance(Point p) {
		return normal.dot(p) + distance;
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

	@Override
	public Iterator<Intersection> intersections(Ray ray) {
		// Determine angle between ray and normal
		final float denom = normal.dot(ray.direction());

		// Orthogonal ray does not intersect
		if(MathsUtil.isZero(denom)) {
			return NONE;
		}

		// Calc intersection distance
		final float d = -distance(ray.origin()) / denom;
		if(d < 0) {
			return NONE;
		}

		// Build intersection result
		return intersection(ray, d);
	}

	/**
	 * Builds an intersection result.
	 */
	private Iterator<Intersection> intersection(Ray ray, float d) {
		return List.of(new Intersection(ray, d, normal)).iterator();
	}

	/**
	 * Creates an adapter for this plane that considers rays in <i>front</i> this plane as <b>not</b> intersecting, i.e. is in the {@link HalfSpace#POSITIVE} half-space.
	 * @return Intersection test for a ray behind this plane
	 */
	public Intersected behind() {
		return ray -> {
			if(halfspace(ray.origin()) == HalfSpace.POSITIVE) {
				return Intersected.NONE;
			}
			else {
				return intersection(ray, 0);
			}
		};
	}
}
