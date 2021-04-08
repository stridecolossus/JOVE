package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;

/**
 * Plane in 3D space.
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

	// TODO
	// - invert?
	// - ctors for XYZ planes and XY XZ YZ etc?

	/**
	 * Creates a plane from the given triangle of points.
	 * @param a
	 * @param b
	 * @param c
	 * @return Plane
	 */
	public static Plane of(Point a, Point b, Point c) {
		final Vector u = Vector.between(a, b);
		final Vector v = Vector.between(b, c);
		final Vector normal = u.cross(v).normalize();
		final float dist = -a.toVector().dot(normal);
		return new Plane(normal, dist);
	}

	/**
	 * Creates a plane given a normal and a point on the plane.
	 * @param normal		Plane normal
	 * @param pt			Point on the plane
	 * @return Plane
	 */
	public static Plane of(Vector normal, Point pt) {
		return new Plane(normal, -pt.toVector().dot(normal));
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
	public float distanceTo(Point pt) {
		return normal.dot(pt.toVector()) - distance;
	}

	/**
	 * Determines on which side of this plane the given point lies.
	 * @param pt Point
	 * @return Side
	 */
	public Side side(Point pt) {
		final float d = distanceTo(pt);
		if(d < 0) {
			return Side.BACK;
		}
		else
		if(d > 0) {
			return Side.FRONT;
		}
		else {
			return Side.INTERSECT;
		}
	}
}
