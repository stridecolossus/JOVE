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
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a compound object comprised of a collection of {@link Component} such as vertex positions, normals, texture coordinates, etc.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Creates a vertex from the given array of components.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(Component... components) {
		return new Vertex(Arrays.asList(components));
	}

	private final List<Component> components;

	/**
	 * Constructor.
	 * @param components Vertex components
	 */
	public Vertex(List<Component> components) {
		this.components = List.copyOf(components);
	}

	/**
	 * @return Components of this vertex
	 */
	public List<Component> components() {
		return components;
	}

	/**
	 * Convenience helper.
	 * @return Component layouts
	 */
	public Stream<Layout> layout() {
		return components.stream().map(Component::layout);
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
		return (T) components.get(index);
	}

	@Override
	public int length() {
		return components.stream().mapToInt(Component::length).sum();
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Component obj : components) {
			obj.buffer(buffer);
		}
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Vertex that) && this.components.equals(that.components);
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
			final var components = Stream.of(pos, normal, coord, col).filter(Objects::nonNull).collect(toList());
			return new Vertex(components);
		}
	}
}
