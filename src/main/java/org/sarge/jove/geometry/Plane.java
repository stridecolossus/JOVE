package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;

/**
 * Plane in 3D space defined by a normal and distance from the origin.
 * @author Sarge
 */
public class Plane {
	/**
	 * Sides of the plane.
	 */
	public static enum Side {
		/**
		 * Object is in-front of the plane.
		 */
		FRONT,

		/**
		 * Object is behind the plane.
		 */
		BACK,

		/**
		 * Object is on the plane.
		 */
		INTERSECT
	}

	private final Vector normal;
	private final float d;

	/**
	 * Constructor.
	 * @param normal	Normal
	 * @param d			Distance from origin
	 */
	public Plane(Vector normal, float d) {
		this.normal = normal.normalize();
		this.d = d;
	}

	/**
	 * Constructor given a point on the plane.
	 * @param normal	Normal
	 * @param pt		Point on the plane
	 */
	public Plane(Vector normal, Point pt) {
		this(normal, -pt.dot(normal));
	}

	/**
	 * Constructor given three points in the plane.
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public Plane(Point p1, Point p2, Point p3) {
		// Calc two edges of the triangle of points
		final Vector u = Vector.between(p1, p2);
		final Vector v = Vector.between(p2, p3);

		// Init plane
		this.normal = u.cross(v).normalize();
		this.d = -p1.dot(normal);
	}

	/**
	 * @return Plane normal
	 */
	public Vector getNormal() {
		return normal;
	}

	/**
	 * @return Distance from origin
	 */
	public float getDistance() {
		return d;
	}

	/**
	 * @param pt Point
	 * @return Distance from this plane to the given point
	 * @see #getSide(Point)
	 */
	public float distanceTo(Point pt) {
		return normal.dot(pt) - d;
	}

	/**
	 * Calculates the intersection point of this plane with the given ray.
	 * @param ray Intersecting ray
	 * @return Intersection point or <tt>null</tt> if not intersecting
	 */
	public Point intersect(Ray ray) {
		// Determine whether ray is co-planar
		final float dot = normal.dot(ray.getDirection());
		if(MathsUtil.isZero(dot)) {
			return null;
		}

		// Check whether ray behind plane
		final float dist = distanceTo(ray.getOrigin());
		final float ratio = -dist / dot;
		if(ratio < MathsUtil.EPSILON) {
			return null;
		}

		// Calc intersection point
		return ray.getOrigin().add(ray.getDirection().multiply(ratio));
	}

	/**
	 * Determines which side of the plane the given point is on.
	 * @param pt Point being tested
	 * @return Plane side or {@link Side#Intersect} if the point is on the plane
	 * @see #distanceTo(Point)
	 */
	public Side getSide(Point pt) {
		final float dist = distanceTo(pt);
		if(dist < 0) {
			return Side.BACK;
		}
		else if(dist > 0) {
			return Side.FRONT;
		}
		else {
			return Side.INTERSECT;
		}
	}

	@Override
	public String toString() {
		return normal + "(" + d + ")";
	}
}
