package org.sarge.jove.model;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.jove.geometry.*;

/**
 * TODO
 * @author Sarge
 */
public record Triangle(List<Vertex> vertices) {
	private static final float THIRD = 1f / 3;

	/**
	 * Constructor.
	 * @param vertices Triangle vertices
	 */
	public Triangle {
		vertices = List.copyOf(vertices);
	}

	/**
	 * Constructor.
	 */
	public Triangle(Vertex a, Vertex b, Vertex c) {
		this(List.of(a, b, c));
	}

	/**
	 * @return Centre point of this triangle
	 */
	public Point centre() {
		return vertices
				.stream()
				.map(Vertex::position)
				.reduce(Point.ORIGIN, Point::add)
				.multiply(THIRD);
	}

	/**
	 * @return Normal of this triangle
	 */
	public Normal normal() {
		final Vector u = edge(0, 1);
		final Vector v = edge(0, 2);
		return u.cross(v).normalize();
	}

	private Vector edge(int start, int end) {
		final Point a = vertices.get(start).position();
		final Point b = vertices.get(end).position();
		return Vector.between(a, b);
	}

	/**
	 * @return Triangle winding order
	 */
	public WindingOrder winding() {
		return null;
	}

	/**
	 * @return Edges of this triangle
	 */
	public Stream<Vector> edges() {
		return Stream.of(edge(0, 1), edge(1, 2));
	}
	// TODO - closed?
}
