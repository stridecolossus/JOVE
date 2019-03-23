package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.texture.TextureCoordinate;
import org.sarge.lib.util.Converter;

/**
 * A <i>vertex</i> is a mutable descriptor for vertex attributes in a {@link Model}.
 * @author Sarge
 */
public class Vertex {
	/**
	 * Descriptor for a component of a vertex.
	 */
	public static final class Component {
		/**
		 * Vertex position component.
		 */
		public static final Component POSITION = new Component(Point.SIZE, Vertex::position);

		/**
		 * Normal component.
		 */
		public static final Component NORMAL = new Component(Vector.SIZE, Vertex::normal);

		/**
		 * 2D texture coordinate component.
		 * TODO - assumes is 2D!
		 */
		public static final Component TEXTURE_COORDINATE = new Component(2, Vertex::coords);

		/**
		 * Colour component.
		 */
		public static final Component COLOUR = new Component(Colour.SIZE, Vertex::colour);

		/**
		 * Converter for a string-representation of vertex components.
		 */
		public static final Converter<List<Component>> CONVERTER = line -> {
			return line.toUpperCase().chars().mapToObj(Component::of).collect(toList());
		};

		/**
		 * Maps a character to a component.
		 * @param ch Character
		 * @return Component
		 * TODO - multiple texture coordinates? 0..8 = texture units? what is more than 8?
		 */
		private static Component of(int ch) {
			switch(ch) {
			case 'V':	return POSITION;
			case 'N':	return NORMAL;
			case 'T':	return TEXTURE_COORDINATE;
			case 'C':	return COLOUR;
			default:	throw new NumberFormatException("Invalid vertex component: " + ch);
			}
		}

		private final int size;
		private final Function<Vertex, Bufferable> mapper;

		/**
		 * Constructor.
		 * @param size			Component size
		 * @param mapper		Maps a vertex to a component
		 */
		public Component(int size, Function<Vertex, Bufferable> mapper) {
			this.size = oneOrMore(size);
			this.mapper = notNull(mapper);
		}

		/**
		 * @return Size of this component
		 */
		public int size() {
			return size;
		}
	}

	private Point pos;
	private Vector normal;
	private TextureCoordinate coords;
	private Colour col;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public Vertex(Point pos) {
		position(pos);
	}

	/**
	 * @return Vertex position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Sets the position of this vertex.
	 * @param pos Vertex position
	 */
	public Vertex position(Point pos) {
		this.pos = notNull(pos);
		return this;
	}

	/**
	 * @return Vertex normal
	 */
	public Vector normal() {
		return normal;
	}

	/**
	 * Sets the normal of this vertex.
	 * @param normal Vertex normal
	 */
	public Vertex normal(Vector normal) {
		this.normal = notNull(normal);
		return this;
	}

	/**
	 * @return Vertex texture coordinates
	 */
	public TextureCoordinate coords() {
		return coords;
	}

	/**
	 * Sets the texture coordinates of this vertex.
	 * @param coords Texture coordinates
	 */
	public Vertex coords(TextureCoordinate coords) {
		this.coords = notNull(coords);
		return this;
	}

	/**
	 * @return Vertex colour
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * Sets the colour of this vertex.
	 * @param col Vertex colour
	 */
	public Vertex colour(Colour col) {
		this.col = notNull(col);
		return this;
	}

	/**
	 * Checks this vertex against the given component specification.
	 * @param components Components
	 * @return Whether this vertex contains the given components
	 */
	public boolean matches(Collection<Component> components) {
		return components.stream().map(c -> c.mapper.apply(this)).allMatch(Objects::nonNull);
	}

	/**
	 * Retrieves the given component in this vertex.
	 * @param c Component
	 * @return Vertex component or <tt>null</tt> if not present
	 */
	public Bufferable map(Component c) {
		return c.mapper.apply(this);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
