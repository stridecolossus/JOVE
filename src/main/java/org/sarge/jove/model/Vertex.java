package org.sarge.jove.model;

import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.Check;

/**
 * A <i>vertex</i> is comprised of a vertex position, normal, colour and texture coordinates.
 * @author Sarge
 */
public interface Vertex {
	/**
	 * @return Vertex position
	 */
	Point position();

	/**
	 * @return Normal
	 */
	Vector normal();

	/**
	 * @return Texture coordinates
	 */
	TextureCoordinate coords();

	/**
	 * @return Colour
	 */
	Colour colour();

	/**
	 * Creates a simple vertex at the given position.
	 * @param pos Vertex position
	 * @return New vertex
	 */
	static Vertex of(Point pos) {
		return new DefaultVertex(pos, null, null, null);
	}

	/**
	 * Default implementation.
	 */
	record DefaultVertex(Point position, Vector normal, TextureCoordinate coords, Colour colour) implements Vertex {
		// Empty
	}

	/**
	 * A <i>vertex component</i> refers to a property of a vertex.
	 */
	enum Component {
		POSITION(Point.SIZE, Vertex::position),
		NORMAL(Vector.SIZE, Vertex::normal),
		TEXTURE_COORDINATE(TextureCoordinate.Coordinate2D.SIZE, Vertex::coords),
		COLOUR(Colour.SIZE, Vertex::colour);

		private final int size;
		private final Function<Vertex, Bufferable> mapper;

		/**
		 * Constructor.
		 * @param size			Component size
		 * @param mapper		Extractor
		 */
		private Component(int size, Function<Vertex, Bufferable> mapper) {
			this.size = size;
			this.mapper = mapper;
		}

		/**
		 * @return Size of this component (number of floating-point values)
		 */
		public int size() {
			return size;
		}

		/**
		 * Maps the given vertex to this component.
		 * @param vertex Vertex
		 * @return Component or {@code null} if not present
		 */
		public Bufferable map(Vertex vertex) {
			return mapper.apply(vertex);
		}
	}

	/**
	 * A <i>vertex layout</i> specifies the component layout of vertices.
	 */
	class Layout {
		private final List<Component> layout;
		private final int size;

		/**
		 * Constructor.
		 * @param layout Component layout
		 * @throws IllegalArgumentException if the layout is empty or contains a duplicate component
		 */
		public Layout(List<Component> layout) {
			Check.notEmpty(layout);
			if(layout.size() != new HashSet<>(layout).size()) throw new IllegalArgumentException("Layout cannot contain duplicate components: " + layout);
			this.layout = List.copyOf(layout);
			this.size = layout.stream().mapToInt(Component::size).sum();
		}

		/**
		 * Constructor.
		 * @param layout Component layout
		 * @throws IllegalArgumentException if the layout is empty or contains a duplicate component
		 */
		public Layout(Component... layout) {
			this(Arrays.asList(layout));
		}

		/**
		 * @return Layout
		 */
		public List<Component> components() {
			return layout;
		}

		/**
		 * @return Total size of this layout (number of floating-point values)
		 */
		public int size() {
			return size;
		}

		/**
		 * Tests whether the components of the given vertex matches this layout.
		 * @param vertex Vertex
		 * @return Whether vertex matches this layout
		 */
		public boolean matches(Vertex vertex) {
			for(Component c : layout) {
				if(c.map(vertex) == null) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Layout that) && this.layout.equals(that.layout);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	/**
	 * Builder for a vertex.
	 */
	class Builder {
		private Point pos;
		private Vector normal;
		private TextureCoordinate coords;
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
			this.normal = normal;
			return this;
		}

		/**
		 * Sets the texture coordinate of this vertex.
		 * @param coords Texture coordinate
		 */
		public Builder coords(TextureCoordinate coords) {
			this.coords = coords;
			return this;
		}

		/**
		 * Sets the vertex colour.
		 * @param col Vertex colour
		 */
		public Builder colour(Colour col) {
			this.col = col;
			return this;
		}

		/**
		 * Constructs this vertex.
		 * @return New vertex
		 */
		public Vertex build() {
			return new DefaultVertex(pos, normal, coords, col);
		}
	}
}
