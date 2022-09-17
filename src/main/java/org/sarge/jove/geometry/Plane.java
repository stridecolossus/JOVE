package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtil;

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
	 * @throws IllegalArgumentException if the triangle is degenerate
	 */
	public static Plane of(Point a, Point b, Point c) {
		final Vector u = Vector.between(a, c);
		final Vector v = Vector.between(b, c);
		if(u.equals(v)) throw new IllegalArgumentException("Triangle points cannot be degenerate");
		final Vector normal = u.cross(v);
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
		normal = normal.normalize();
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
	 * @param p Point
	 * @return Half-space
	 * @see HalfSpace#of(float)
	 */
	public HalfSpace halfspace(Point p) {
		return HalfSpace.of(distance(p));
	}

	@Override
	public Intersection intersection(Ray ray) {
		return intersections(ray, true);
	}

	/**
	 * Determines the intersections of the given ray in the specified half-space of this plane.
	 * @param ray Ray
	 * @param pos Whether rays originating in the {@link HalfSpace#POSITIVE} half-space are subject to the intersection test
	 * @return Intersections
	 */
	private Intersection intersections(Ray ray, boolean pos) {
		// Determine angle between ray and normal
		final float denom = normal.dot(ray.direction());

		// Orthogonal ray does not intersect
		if(MathsUtil.isZero(denom)) {
			return Intersection.NONE;
		}

		// Calc intersection distance
		final float d = distance(ray.origin());
		final float t = -d / denom;

		// Check for intersection
		if(pos) {
			if(t < 0) {
				return Intersection.NONE;
			}
		}
		else {
			if(d > 0) {
				return Intersection.NONE;
			}
		}

		// Build intersection result
		return Intersection.of(t, normal);
	}

	/**
	 * Creates an adapter for this plane that only applies the intersection test to rays <i>behind</i> this plane, i.e. in the {@link HalfSpace#NEGATIVE} half-space.
	 * @return Intersecting surface for rays behind this plane
	 * @see #halfspace(HalfSpace)
	 */
	public Intersected behind() {
		return ray -> intersections(ray, false);
	}

	/**
	 * Creates an adapter for this plane that considers <b>all</b> rays in the given half-space as intersecting.
	 * <p>
	 * This implementation offers better performance when the actual intersection point and surface normal are not relevant.
	 * Note that the intersection results are {@link Intersected#UNDEFINED}.
	 * <p>
	 * @return Half-space intersection test
	 * @see #behind()
	 */
	public Intersected halfspace(HalfSpace space) {
		return ray -> {
			if(halfspace(ray.origin()) == space) {
				return Intersection.UNDEFINED;
			}
			else {
				return Intersection.NONE;
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Plane that) &&
				this.normal.equals(that.normal) &&
				MathsUtil.isEqual(this.distance, that.distance);
	}
}
