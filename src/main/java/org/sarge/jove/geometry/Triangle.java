package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.jove.geometry.Ray.*;

/**
 * A <i>triangle</i> is a polygon with three vertices.
 * @author Sarge
 */
public record Triangle(Point a, Point b, Point c) implements Intersected {
	/**
	 * Constructor.
	 * @param vertices Triangle vertices
	 * @throws IllegalArgumentException if the given vertices do not form a triangle
	 */
	public Triangle(List<Point> vertices) {
		this(vertices.get(0), vertices.get(1), vertices.get(2));
		if(vertices.size() != 3) throw new IllegalArgumentException("Invalid number of triangle vertices");
	}

	/**
	 * Constructor.
	 */
	public Triangle {
		notNull(a);
		notNull(b);
		notNull(c);
	}

	/**
	 * Calculates the centre point of this triangle, i.e. the average of the vertices.
	 * @return Triangle centre
	 */
	public Point centre() {
		return a.add(b).add(c).multiply(1 / 3f);
	}

	/**
	 * Calculates the normal of this triangle.
	 * @return Triangle normal
	 */
	public Vector normal() {
		final Vector u = Vector.between(a, b);
		final Vector v = Vector.between(a, c);
		return u.cross(v);
	}

	/**
	 * @return Whether this triangle is <i>degenerate</i> (has a zero area)
	 */
	public boolean isDegenerate() {
		return a.equals(b) || a.equals(c);
	}

	/**
	 * Determines the winding order (or orientation) of this polygon with respect to the given view axis (usually {@link Axis#Z}).
	 * @param axis Axis
	 * @return Winding order
	 * @see WindingOrder#of(float)
	 */
	public WindingOrder winding(Vector axis) {
		final float dot = axis.dot(this.normal());
		return WindingOrder.of(dot);
	}

	@Override
	public Intersection intersection(Ray ray) {
//		// Determine angle between ray and triangle
//		final Normal normal = this.normal().normalize();
//		final float denom = normal.dot(ray.direction());
//
//		// Orthogonal ray does not intersect
//		if(MathsUtil.isZero(denom)) {
//			return Intersection.NONE;
//		}
//
//		// Calc distance along ray
//		final float d = -normal.dot(ray.origin()) / denom;
//
//		// Check for ray behind
//		if(d < 0) {
//			return Intersection.NONE;
//		}
//
//		// Build intersection result
//		return Intersection.of(d, normal);

		throw new UnsupportedOperationException(); // TODO
	}
}
