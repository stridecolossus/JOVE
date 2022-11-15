package org.sarge.jove.geometry;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.*;

import org.sarge.jove.geometry.Ray.*;

/**
 * A <i>polygon</i> comprises a number of vertices.
 * <p>
 * Note this implementation assumes (but does not enforce) that the polygon vertices are <i>open</i>, i.e. the final vertex does <b>not</b> duplicate the starting point.
 * See {@link #close()}
 * <p>
 * @author Sarge
 */
public class Polygon implements Intersected {
	private final List<Point> vertices;

	/**
	 * Constructor.
	 * @param vertices Polygon vertices
	 * @throws IllegalArgumentException if {@link #vertices} does not have at least 3 vertices
	 */
	public Polygon(List<Point> vertices) {
		if(vertices.size() < 3) throw new IllegalArgumentException("Polygon must have at least 3 vertices");
		this.vertices = List.copyOf(vertices);
	}

	/**
	 * @return Polygon vertices
	 */
	public List<Point> vertices() {
		return vertices;
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
	 * @return Edges of this polygon
	 */
	public Stream<Vector> edges() {
		return IntStream
				.range(0, vertices.size() - 1)
				.mapToObj(this::edge);
	}

	/**
	 * Builds a vertex edge between the given vertices.
	 */
	protected Vector edge(int index) {
		return Vector.between(vertices.get(index), vertices.get(index + 1));
	}

	/**
	 * Calculates the normal of this polygon.
	 * @return Polygon normal
	 */
	public Vector normal() {
		// Enumerate edges
		final List<Vector> edges = this.edges().toList();

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
				.reduce(new Vector(0, 0, 0), Vector::add);
	}

	/**
	 * Closes this polygon, i.e. duplicates the first vertex at the end of the polygon.
	 * @return Closed polygon
	 */
	public Polygon close() {
		final var closed = new ArrayList<>(vertices);
		closed.add(vertices.get(0));
		return new Polygon(closed);
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
	public int hashCode() {
		return vertices.hashCode();
	}

	@Override
	public Intersection intersection(Ray ray) {
		// TODO - how to do this? especially for concave polygons? custom implementation for triangle
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Polygon that) &&
				this.vertices.equals(that.vertices);
	}

	@Override
	public String toString() {
		return vertices.toString();
	}
}
