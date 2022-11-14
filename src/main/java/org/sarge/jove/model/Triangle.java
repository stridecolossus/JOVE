package org.sarge.jove.model;

import java.util.List;

import org.sarge.jove.geometry.*;

/**
 * A <i>triangle</i> is a convenience sub-class for a polygon with three vertices.
 * @author Sarge
 */
public class Triangle extends Polygon {
	/**
	 * Constructor.
	 * @param vertices Triangle vertices
	 * @throws IllegalArgumentException if the given vertices do not form a triangle
	 */
	public Triangle(List<Point> vertices) {
		super(vertices);
		if(vertices.size() != 3) throw new IllegalArgumentException("Invalid number of triangle vertices");
	}

	/**
	 * Constructor.
	 */
	public Triangle(Point a, Point b, Point c) {
		this(List.of(a, b, c));
	}

	@Override
	public Normal normal() {
		final Vector u = edge(0);
		final Vector v = edge(1);
		return u.cross(v).normalize();
	}

	/**
	 * @return Whether this triangle is <i>degenerate</i> (has a zero area)
	 */
	public boolean isDegenerate() {
		final var vertices = this.vertices();
		final Point p = vertices.get(0);
		return p.equals(vertices.get(1)) || p.equals(vertices.get(2));
	}
}
