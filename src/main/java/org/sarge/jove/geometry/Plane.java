package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
 * For example <code>new Plane(Axis.Y, 1)</code> creates the plane in X-Z at Y = <b>minus</b> one.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Plane_(geometry)">Wikipedia</a>
 * @author Sarge
 */
public final class Plane implements Intersected {
	/**
	 * The <i>half space</i> defines the <i>sides</i> of this plane with respect to the normal.
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
		final Normal normal = u.cross(v).normalize();
		return of(normal, a);
	}

	/**
	 * Creates a plane given a normal and a point on the plane.
	 * @param n Plane normal
	 * @param p Point on the plane
	 * @return New plane
	 */
	public static Plane of(Normal n, Point p) {
		final float d = -n.dot(p);
		return new Plane(n, d);
	}

	private final Normal normal;
	private final float dist;

	/**
	 * Constructor.
	 * @param normal		Plane normal
	 * @param dist			Distance of the plane from the origin
	 */
	public Plane(Normal normal, float dist) {
		this.normal = notNull(normal);
		this.dist = dist;
	}

	/**
	 * @return Plane normal
	 */
	public Normal normal() {
		return normal;
	}

	/**
	 * @return Distance of this plane from the origin
	 */
	public float distance() {
		return dist;
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
			final Normal n = normal.multiply(inv).normalize();
			return new Plane(n, dist * inv);
		}
	}
	// TODO - what is this actually doing? need references, is it used anyway?

	/**
	 * Determines the distance of the given point from this plane.
	 * @param p Point
	 * @return Distance to the given point
	 */
	public float distance(Point p) {
		return normal.dot(p) + dist;
	}

	/**
	 * Helper - Determines the half space of the given point with respect to this plane.
	 * @param p Point
	 * @return Half space
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
	 * Determines the intersections of the given ray in the specified half space of this plane.
	 * @param ray Ray
	 * @param pos Whether rays originating in the {@link HalfSpace#POSITIVE} half space are subject to the intersection test
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
	 * Creates an adapter for this plane that only applies the intersection test to rays <i>behind</i> this plane, i.e. in the {@link HalfSpace#NEGATIVE} half space.
	 * @return Intersecting surface for rays behind this plane
	 * @see #halfspace(HalfSpace)
	 */
	public Intersected behind() {
		return ray -> intersections(ray, false);
	}

	/**
	 * Creates an adapter for this plane that considers <b>all</b> rays in the given half space as intersecting.
	 * <p>
	 * This implementation may offer better performance where the actual intersection point and surface normal are not relevant.
	 * Note that the intersection results are either {@link Intersection#NONE} or {@link Intersection#UNDEFINED}.
	 * <p>
	 * @return Half space intersection test
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
	public int hashCode() {
		return Objects.hash(normal, dist);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Plane that) &&
				this.normal.equals(that.normal) &&
				MathsUtil.isEqual(this.dist, that.dist);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(normal)
				.append("d", dist)
				.build();
	}
}
