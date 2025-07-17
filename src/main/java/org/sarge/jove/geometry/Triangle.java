package org.sarge.jove.geometry;

import java.util.*;

import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>triangle</i> is a polygon with three vertices.
 * @author Sarge
 */
public record Triangle(List<Point> vertices) implements IntersectedSurface {
	/**
	 * Constructor.
	 * @param vertices Triangle vertices
	 * @throws IllegalArgumentException if the triangle does have exactly three vertices
	 */
	public Triangle {
		if(vertices.size() != 3) throw new IllegalArgumentException();
		vertices = List.copyOf(vertices);
	}

	/**
	 * Convenience constructor given an array of points.
	 * @param vertices Triangle vertices
	 * @throws IllegalArgumentException if the triangle does have exactly three vertices
	 */
	public Triangle(Point... vertices) {
		this(Arrays.asList(vertices));
	}

	/**
	 * Calculates the centre point of this triangle, i.e. the average of the vertices.
	 * @return Triangle centre
	 */
	public Point centre() {
		final Vector a = vector(0);
		final Vector b = vector(1);
		final Vector c = vector(2);
		final Vector result = a.add(b).add(c).multiply(1 / 3f);
		return new Point(result);
	}

	private Vector vector(int index) {
		return new Vector(vertices.get(index));
	}

	/**
	 * Calculates the normal of this triangle.
	 * @return Triangle normal
	 */
	public Vector normal() {
		final Vector u = edge(1);
		final Vector v = edge(2);
		return u.cross(v);
	}

	private Vector edge(int index) {
		return Vector.between(vertices.get(0), vertices.get(index));
	}

	/**
	 * @return Whether this triangle is <i>degenerate</i> (has zero area)
	 */
	public boolean isDegenerate() {
		final Point p = vertices.get(0);
		return p.equals(vertices.get(1)) || p.equals(vertices.get(2));
	}

	/**
	 * Determines the winding order (or orientation) of this polygon with respect to the given view axis (usually {@link Axis#Z}).
	 * @param axis Axis
	 * @return Winding order
	 */
	public WindingOrder winding(Vector axis) {
		final float determinant = axis.dot(this.normal());
		if(determinant > 0) {
			return WindingOrder.COUNTER_CLOCKWISE;
		}
		else
		if(determinant < 0) {
			return WindingOrder.CLOCKWISE;
		}
		else {
			return WindingOrder.COLINEAR;
		}
	}

	@Override
	public Iterable<Intersection> intersections(Ray ray) {
		// Determine angle between ray and triangle
		final Normal normal = new Normal(this.normal());
		final float denom = normal.dot(ray.direction());

		// Orthogonal ray does not intersect
		if(MathsUtility.isApproxZero(denom)) {
			return EMPTY_INTERSECTIONS;
		}

		// Calc distance along ray
		final float d = -normal.dot(new Vector(ray.origin())) / denom;

		// Check for ray behind
		if(d < 0) {
			return EMPTY_INTERSECTIONS;
		}

		// Build intersection result
		return List.of(ray.intersection(d, normal));
	}
}
