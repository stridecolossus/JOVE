package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>plane</i> defines a flat surface in 3D space.
 * <p>
 * Mathematically the <i>general form</i> of a plane is: {@code ax + by + cz + d = 0}
 * where {@code n = (a, b, c)} is the plane normal and {@code d} is the distance of the plane from the origin.
 * <p>
 * Note that the distance increases in the <i>opposite</i> direction to the normal vector.
 * <br>
 * For example {@code Plane(Axis.Y, 1)} defines X-Z plane at Y = <b>minus</b> one.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/Plane_(geometry)">Wikipedia</a>
 * @author Sarge
 */
public record Plane(Normal normal, float distance) implements IntersectedSurface {
	/**
	 * Creates a plane from the given triangle of points.
	 * @param triangle Triangle
	 * @return New plane
	 * @throws IllegalArgumentException if the given triangle {@link Triangle#isDegenerate()}
	 */
	public static Plane of(Triangle triangle) {
		if(triangle.isDegenerate()) {
			throw new IllegalArgumentException("Cannot define a plane from a degenerate triangle");
		}
		final Normal normal = new Normal(triangle.normal());
		return new Plane(normal, triangle.a());
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
	 * @param normal		Plane normal
	 * @param point			Point on the plane
	 */
	public Plane(Normal normal, Point point) {
		this(normal, -normal.dot(new Vector(point)));
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
				// TODO - test approx equals as first case?
			}
		}
	}

	/**
	 * Helper.
	 * Determines the half space of the given point with respect to this plane.
	 * @param p Point
	 * @return Half space
	 * @see HalfSpace#of(float)
	 */
	public HalfSpace halfspace(Point p) {
		return HalfSpace.of(distance(p));
	}

	@Override
	public List<Intersection> intersections(Ray ray) {
		// Determine angle between ray and normal
		final float determinant = normal.dot(ray.direction());

		// Orthogonal ray does not intersect
		if(MathsUtility.isApproxZero(determinant)) {
			return EMPTY_INTERSECTIONS;
		}

		// Calculate closest intersection distance
		final float dist = -distance(ray.origin()) / determinant;

		// Check whether intersects
		if((dist < 0) || (dist * dist > ray.direction().magnitude())) {
			return EMPTY_INTERSECTIONS;
		}

		// Build intersection result
		return List.of(new Intersection(dist, this));
	}

	@Override
	public Normal normal(Point intersection) {
		return normal;
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
