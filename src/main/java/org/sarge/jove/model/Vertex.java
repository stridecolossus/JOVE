package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a compound object comprised of:
 * <ul>
 * <li>vertex position</li>
 * <li>normal</li>
 * <li>texture coordinate</li>
 * <li>colour</li>
 * </ul>
 * <p>
 * Notes that all components are optional except for the vertex position.
 * <p>
 * The {@link Component} enumeration specifies the elements comprising a vertex and is used to {@link #transform(List)} a vertex to a different layout.
 * <p>
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Vertex components.
	 */
	public enum Component {
		POSITION(Point.LAYOUT, Vertex::position, Builder::position),
		NORMAL(Vector.LAYOUT, Vertex::normal, Builder::normal),
		COORDINATE(Coordinate2D.LAYOUT, Vertex::coordinate, Builder::coordinate),
		COLOUR(Colour.LAYOUT, Vertex::colour, Builder::colour);

		/**
		 * Default vertex components.
		 */
		public static final List<Component> DEFAULT = Arrays.asList(Component.values());

		private final Layout layout;
		private final Function<Vertex, ? extends Bufferable> mapper;
		private final BiConsumer<Builder, Bufferable> setter;

		/**
		 * Constructor.
		 * @param <T> Component type
		 * @param layout		Component layout
		 * @param mapper		Extracts this component from a vertex
		 * @param setter		Populates this component to a builder
		 */
		@SuppressWarnings("unchecked")
		private <T extends Bufferable> Component(Layout layout, Function<Vertex, T> mapper, BiConsumer<Builder, T> setter) {
			this.layout = layout;
			this.mapper = mapper;
			this.setter = (BiConsumer<Builder, Bufferable>) setter;
		}

		/**
		 * @return Layout of this component
		 */
		public Layout layout() {
			return layout;
		}

		/**
		 * Applies this component to the given vertex.
		 * @param vertex		Vertex
		 * @param builder		Output builder
		 */
		private void apply(Vertex vertex, Builder builder) {
			final Bufferable obj = mapper.apply(vertex);
			if(obj == null) throw new IllegalArgumentException(String.format("Vertex component cannot be NULL: this=%s vertex=%s", this, vertex));
			setter.accept(builder, obj);
		}
	}

	private final Point pos;
	private final Vector normal;
	private final Coordinate coord;
	private final Colour col;

	/**
	 * Constructor.
	 * @param pos			Vertex position
	 * @param normal		Normal
	 * @param coord			Texture coordinate
	 * @param col			Colour
	 */
	public Vertex(Point pos, Vector normal, Coordinate coord, Colour col) {
		this.pos = notNull(pos);
		this.normal = normal;
		this.coord = coord;
		this.col = col;
	}

	/**
	 * Convenience constructor for a vertex comprised only of a point.
	 * @param pos Vertex position
	 */
	public Vertex(Point pos) {
		this(pos, null, null, null);
	}

	/**
	 * @return Vertex position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * @return Optional normal
	 */
	public Vector normal() {
		return normal;
	}

	/**
	 * @return Optional texture coordinate
	 */
	public Coordinate coordinate() {
		return coord;
	}

	/**
	 * @return Optional colour
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * @return Vertex components
	 */
	public Stream<Bufferable> stream() {
		return Stream
				.of(pos, normal, coord, col)
				.filter(Objects::nonNull);
	}

	/**
	 * Transforms this vertex to the given component layout.
	 * @param transform Vertex component transform
	 * @return Transformed vertex
	 */
	public Vertex transform(List<Component> transform) {
		final Builder builder = new Builder();
		for(Component c : transform) {
			c.apply(this, builder);
		}
		return builder.build();
	}

	@Override
	public int length() {
		return stream().mapToInt(Bufferable::length).sum();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		stream().forEach(c -> c.buffer(bb));
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, normal, coord, col);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				this.pos.equals(that.pos) &&
				Objects.equals(this.normal, that.normal) &&
				Objects.equals(this.coord, that.coord) &&
				Objects.equals(this.col, that.col);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(normal)
				.append(coord)
				.append(col)
				.build();
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
		 * @param coord Texture coordinate
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
		 * @throws IllegalArgumentException if a vertex position has not been specified
		 */
		public Vertex build() {
			if(pos == null) throw new IllegalArgumentException("Vertex position is mandatory");
			return new Vertex(pos, normal, coord, col);
		}
	}
}
