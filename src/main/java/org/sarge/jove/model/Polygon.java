package org.sarge.jove.model;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.*;

import org.sarge.jove.geometry.*;

/**
 * A <i>polygon</i> comprises a number of vertices.
 * Note this implementation assumes (but does not enforce) that the polygon vertices are <i>open</i>, i.e. the final vertex does <b>not</b> duplicate the starting point.
 * @author Sarge
 */
public record Polygon(List<Point> vertices) {
	/**
	 * Constructor.
	 * @param vertices Polygon vertices
	 * @throws IllegalArgumentException if {@link #vertices} does not have at least 3 vertices
	 */
	public Polygon {
		if(vertices.size() < 3) throw new IllegalArgumentException("Polygon must have at least 3 vertices");
		vertices = List.copyOf(vertices);
	}

	/**
	 * Calculates the centre point of the polygon, i.e. the average of the vertices.
	 * @return Polygon centre
	 */
	public Point centre() {
		return vertices
				.stream()
				.reduce(Point.ORIGIN, Point::add)
				.multiply(1f / vertices.size());
	}

	/**
	 * Builds the <i>edges</i> of this polygon, where an edge is the vector between successive vertices.
	 * @param close Whether to include the <i>closing</i> edge, i.e. the edge between the first and last vertices
	 * @return Edges of this polygon
	 */
	public Stream<Vector> edges(boolean close) {
		final int last = vertices.size() - 1;
		final var edges = IntStream
				.range(0, last)
				.mapToObj(n -> edge(n, n + 1));

		if(close) {
			return Stream.concat(edges, Stream.of(edge(last, 0)));
		}
		else {
			return edges;
		}
	}

	/**
	 * Builds a vertex edge between the given vertices.
	 */
	private Vector edge(int start, int end) {
		return Vector.between(vertices.get(start), vertices.get(end));
	}

	/**
	 * Calculates the normal of this polygon.
	 * @return Polygon normal
	 */
	public Normal normal() {
		// Enumerate edges
		final List<Vector> edges = this.edges(false).toList();

		// Generate the normal of each pair of edges
		final IntFunction<Vector> cross = n -> {
			final Vector a = edges.get(n);
			final Vector b = edges.get(n + 1);
			return a.cross(b);
		};

		// Sum the edge normals
		return IntStream
				.range(0, edges.size() - 1)
				.mapToObj(cross)
				.reduce(new Vector(0, 0, 0), Vector::add)
				.normalize();
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
}
