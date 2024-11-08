package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtility;

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
public record Plane(Normal normal, float distance) implements Intersection.Surface {
	/**
	 * Creates a plane from the given triangle of points.
	 * @param triangle Triangle
	 * @return New plane
	 * @throws IllegalArgumentException if the given triangle {@link Triangle#isDegenerate()}
	 */
	public static Plane of(Triangle triangle) {
		if(triangle.isDegenerate()) throw new IllegalArgumentException("Cannot define a plane from a degenerate triangle");
		final Normal normal = new Normal(triangle.normal());
		return new Plane(normal, triangle.vertices().getFirst());
	}

	/**
	 * Constructor.
	 * @param normal		Plane normal
	 * @param distance		Distance of this plane from the origin
	 */
	public Plane {
		requireNonNull(normal);
	}

	/**
	 * Constructor given a normal and a point on the plane.
	 * @param n Plane normal
	 * @param p Point on this plane
	 */
	public Plane(Normal n, Point p) {
		this(n, -n.dot(new Vector(p)));
	}

	/**
	 * Normalizes this plane.
	 * @return Normalized plane
	 */
	public Plane normalize() {
		final float len = normal.magnitude();
		if(MathsUtility.isApproxEqual(len, 1)) {
			return this;
		}
		else {
			final float inv = MathsUtility.inverseSquareRoot(len);
			final Vector n = normal.multiply(inv).normalize();
			return new Plane(new Normal(n), distance * inv);
		}
	}
	// TODO - what is this actually doing? need references, is it used anyway?

	/**
	 * Determines the distance of the given point from this plane.
	 * @param p Point
	 * @return Distance to the given point
	 */
	public float distance(Point p) {
		return normal.dot(new Vector(p)) + distance;
	}

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
	 * Helper - Determines the half space of the given point with respect to this plane.
	 * @param p Point
	 * @return Half space
	 * @see HalfSpace#of(float)
	 */
	public HalfSpace halfspace(Point p) {
		return HalfSpace.of(distance(p));
	}

	@Override
	public Iterable<Intersection> intersections(Ray ray) {
		// Determine angle between ray and normal
		final float denom = normal.dot(ray.direction());

		// Orthogonal ray does not intersect
		if(MathsUtility.isApproxZero(denom)) {
			return Intersection.NONE;
		}

		// Calculate closest intersection distance
		final float dist = -distance(ray.origin()) / denom;

		// Check whether intersects
		if((dist < 0) || (dist * dist > ray.direction().magnitude())) {
			return Intersection.NONE;
		}

		// Build intersection result
		return List.of(ray.intersection(dist, normal));
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Plane that) &&
				this.normal.equals(that.normal) &&
				MathsUtility.isApproxEqual(this.distance, that.distance);
	}
}
