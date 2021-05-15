package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.VertexComponent;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a compound element of a <i>vertex buffer</i> (or VBO).
 * <p>
 * A vertex is essentially a wrapper for an array of {@link VertexComponent} bufferable objects.
 * <p>
 * A {@link Builder} can be used to create a vertex comprised of the common built-in primitives, e.g.
 * <pre>
 * 	Vertex v = new Builder()
 * 		.position(new Point(...))
 * 		.normal(new Vector(...))
 * 		.coordinate(new Coordinate2D(...))
 * 		.colour(new Colour(...))
 * 		.build();
 * </pre>
 * <p>
 * @see Component
 * @see DefaultModel
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Creates a vertex from the given components.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(VertexComponent... components) {
		return new Vertex(components);
	}

	/**
	 * Creates a vertex from the given collection of components.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(List<VertexComponent> components) {
		return new Vertex(components.toArray(VertexComponent[]::new));
	}

	private final VertexComponent[] components;

	/**
	 * Constructor.
	 * @param components Vertex components
	 */
	protected Vertex(VertexComponent[] components) {
		this.components = notNull(components);
	}

	/**
	 * @return Components of this vertex
	 */
	public List<VertexComponent> components() {
		return Arrays.asList(components);
	}

	/**
	 * Retrieves a vertex component by index.
	 * @param <T> Component type
	 * @param index Index
	 * @return Vertex component
	 * @throws ArrayIndexOutOfBoundsException for an invalid index
	 */
	@SuppressWarnings("unchecked")
	public <T extends Bufferable> T get(int index) {
		return (T) components[index];
	}

	/**
	 * @return Layout of this vertex
	 */
	public List<Layout> layout() {
		return Arrays.stream(components).map(VertexComponent::layout).collect(toList());
	}

	@Override
	public int length() {
		return Arrays.stream(components).mapToInt(VertexComponent::length).sum();
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(VertexComponent obj : components) {
			obj.buffer(buffer);
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(components);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Vertex that) && Arrays.equals(this.components, that.components);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(components).build();
	}

	/**
	 * Builder for a vertex.
	 */
	public static class Builder {
		private Point pos;
		private Vector normal;
		private Coordinate coord;
		private Colour col;

		/**
		 * Sets the vertex position.
		 * @param pos Vertex position
		 */
		public Builder position(Point pos) {
			this.pos = notNull(pos);
			return this;
		}

		/**
		 * Sets the vertex normal.
		 * @param normal Vertex normal
		 */
		public Builder normal(Vector normal) {
			this.normal = notNull(normal);
			return this;
		}

		/**
		 * Sets the vertex texture coordinate.
		 * @param coord Vertex coordinate
		 */
		public Builder coordinate(Coordinate coord) {
			this.coord = notNull(coord);
			return this;
		}

		/**
		 * Sets the vertex colour.
		 * @param col Vertex colour
		 */
		public Builder colour(Colour col) {
			this.col = notNull(col);
			return this;
		}

		/**
		 * Constructs this vertex.
		 * @return New vertex
		 */
		public Vertex build() {
			final VertexComponent[] components = Stream
					.of(pos, normal, coord, col)
					.filter(Objects::nonNull)
					.toArray(VertexComponent[]::new);

			return new Vertex(components);
		}
	}
}
