package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>triangle</i> is a polygon comprised of three vertices.
 * @author Sarge
 */
public record Triangle(Point a, Point b, Point c) implements IntersectedSurface {
	/**
	 * Constructor.
	 */
	public Triangle {
		requireNonNull(a);
		requireNonNull(b);
		requireNonNull(c);
	}

	/**
	 * Calculates the centre point of this triangle, i.e. the average of the vertices.
	 * @return Triangle centre
	 */
	public Point centre() {
		final float x = a.x + b.x + c.x;
		final float y = a.y + b.y + c.y;
		final float z = a.z + b.z + c.z;
		final Vector v = new Vector(x, y, z).multiply(1 / 3f);
		return new Point(v);
	}

	/**
	 * Calculates the normal of this triangle.
	 * @return Triangle normal
	 */
	public Normal normal() {
		final Vector u = Vector.between(a, b);
		final Vector v = Vector.between(a, c);
		return new Normal(u.cross(v));
	}

	/**
	 * @return Whether this triangle is <i>degenerate</i> (has zero area)
	 */
	public boolean isDegenerate() {
		return a.equals(b) || a.equals(c);
	}

	/**
	 * {@inheritDoc}
	 * Calculates the intersection point of the given ray with this triangle.
	 * Note that the triangle edges are considered as intersection results by this implementation.
	 * @see <a href="https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm">Wikipedia<a>
	 */
	@Override
	public List<Intersection> intersections(Ray ray) {
		// Determine angle between ray and triangle
		final Vector ab = Vector.between(a, b);
		final Vector ac = Vector.between(a, c);
		final Vector cross = ray.direction().cross(ac);
		final float determinant = ab.dot(cross);

		// Orthogonal ray does not intersect
		if(MathsUtility.isApproxZero(determinant)) {
			return EMPTY_INTERSECTIONS;
		}

		final float inv = 1f / determinant;
		final Vector s = Vector.between(a, ray.origin());
		final float u = inv * s.dot(cross);
		if((u < 0f) || (u > 1f)) {
			return EMPTY_INTERSECTIONS;
		}

		final Vector se = s.cross(ab);
		final float v = inv * ray.direction().dot(se);
		if((v < 0f) || (u + v > 1f)) {
			return EMPTY_INTERSECTIONS;
		}

		// Determine intersection point
		float t = inv * ac.dot(se);
		if(t < 0f) {
			return EMPTY_INTERSECTIONS;
		}

		// Build result
		return List.of(new Intersection(this, t));
	}

	@Override
	public Normal normal(Point intersection) {
		return normal();
	}
}
