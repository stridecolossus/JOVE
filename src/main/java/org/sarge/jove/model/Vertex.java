package org.sarge.jove.model;

import static org.sarge.jove.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.texture.TextureCoordinate;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.Check;

/**
 * A <i>vertex</i> is comprised of a vertex position, normal, colour and texture coordinates.
 * <p>
 * Notes:
 * <ul>
 * <li>only the vertex position is mandatory</li>
 * </ul>
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
	 * Default implementation.
	 */
	record DefaultVertex(Point position, Vector normal, TextureCoordinate coords, Colour colour) implements Vertex {
		/**
		 * Constructor.
		 */
		public DefaultVertex {
			Check.notNull(position);
		}

		/**
		 * Convenience constructor for a vertex that is comprised only of a position.
		 * @param pos Vertex position
		 */
		public DefaultVertex(Point pos) {
			this(pos, null, null, null);
		}
	}

	/**
	 * A <i>vertex component</i> refers to a property of a vertex.
	 * TODO - assumes 2D texture coordinates
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
		 * Extracts this component from a vertex and appends it to the given buffer.
		 * @param vertex		Vertex
		 * @param fb			Destination buffer
		 */
		public void buffer(Vertex vertex, FloatBuffer fb) {
			mapper.apply(vertex).buffer(fb);
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
		 * Helper - Creates and populates an interleaved buffer containing the given vertex data.
		 * @param vertices Vertex data
		 * @return New buffer
		 */
		public ByteBuffer buffer(List<Vertex> vertices) {
			// Create buffer
			final ByteBuffer bb = BufferFactory.byteBuffer(size * Float.BYTES * vertices.size());

			// Buffer vertices
			final FloatBuffer fb = bb.asFloatBuffer();
			vertices.forEach(v -> buffer(v, fb));

			return bb;
		}

		/**
		 * Buffers the components of a vertex to the given buffer according to this layout.
		 * @param vertex		Vertex
		 * @param buffer		Output buffer
		 */
		public void buffer(Vertex vertex, FloatBuffer buffer) {
			layout.forEach(c -> c.buffer(vertex, buffer));
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


//	/**
//	 * A <i>vertex component</i> describes the structure of part of a vertex.
//	 */
//	record Component(Type type, boolean signed, int size, int bytes) {
//		/**
//		 * Component data type.
//		 */
//		public enum Type {
//			INT,
//			FLOAT,
//			NORM,
//			SCALED,
//			SRGB;				// TODO - required?
//
//			/**
//			 * Maps the given image type to the corresponding component type.
//			 * @param type Image type
//			 * @return Component type
//			 */
//			public static Type of(Image.Type type) {
//				switch(type) {
//				case INT:		return Type.INT;
//				case FLOAT:		return Type.FLOAT;
//				case BYTE:		return Type.NORM;
//				default:		throw new RuntimeException();
//				}
//			}
//		}
//
//		/**
//		 * Vertex position component.
//		 */
//		public static final Component POSITION = new Component(Point.SIZE);
//
//		/**
//		 * Normal component.
//		 */
//		public static final Component NORMAL = new Component(Vector.SIZE);
//
//		/**
//		 * Colour component.
//		 */
//		public static final Component COLOUR = new Component(Colour.SIZE);
//
//		/**
//		 * Texture coordinate components with 1..3 dimensions.
//		 */
//		private static final List<Component> TEXTURE_COORDINATE = IntStream.range(1, 4).mapToObj(Component::new).collect(toList());
//
//		/**
//		 * Looks up a texture coordinate component of the given dimension.
//		 * @param dim Dimension 1..3
//		 * @return Texture coordinate component
//		 * @throws IndexOutOfBoundsException if the dimension is not in the range 1..3
//		 */
//		public static Component coordinate(int dim) {
//			return TEXTURE_COORDINATE.get(dim - 1);
//		}
//
//		/**
//		 * Helper - Calculates the total size of the given components.
//		 * @param components Components
//		 * @return Total size
//		 */
//		public static int size(Collection<Component> components) {
//			return components.stream().mapToInt(Component::size).sum();
//		}
//
//		/**
//		 * Constructor.
//		 * @param type		Data type
//		 * @param signed	Whether the data type is signed
//		 * @param size 		Size of this component
//		 * @param bytes		Bytes per component
//		 */
//		public Component {
//			Check.notNull(type);
//			Check.oneOrMore(size);
//			Check.oneOrMore(bytes);
//		}
//
//		/**
//		 * Convenience constructor for a signed floating-point component.
//		 * @param size Size of this component
//		 */
//		public Component(int size) {
//			this(Type.FLOAT, true, size, Float.BYTES);
//		}
//
//		/**
//		 * @return Component size
//		 */
//		public int size() {
//			return size;
//		}
//	}
//
//	/**
//	 * Partial implementation comprised only of a vertex position.
//	 * <p>
//	 * Notes:
//	 * <ul>
//	 * <li>the vertex normal and texture coordinates are initially <tt>null</tt></li>
//	 * <li>{@link AbstractVertex#normal(Vector)} throws {@link UnsupportedOperationException} by default
//	 * </ul>
//	 */
//	abstract class AbstractVertex implements Vertex {
//		protected final Point pos;
//
//		/**
//		 * Constructor.
//		 * @param pos Vertex position
//		 */
//		protected AbstractVertex(Point pos) {
//			this.pos = notNull(pos);
//		}
//
//		@Override
//		public Point position() {
//			return pos;
//		}
//
//		@Override
//		public Vector normal() {
//			return null;
//		}
//
//		@Override
//		public void normal(Vector normal) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public TextureCoordinate coordinates() {
//			return null;
//		}
//
//		// TODO - equals, tostring
//	}
//
//	/**
//	 * A <i>mutable vertex</i> is a default implementation used by model builders.
//	 * <p>
//	 * Notes:
//	 * <ul>
//	 * <li>the vertex normal and texture coordinates are initially {@code null}</li>
//	 * <li>the user is responsible for ensuring that {@code null} components are not passed to {@link #buffer(FloatBuffer)}</li>
//	 * </ul>
//	 */
//	class MutableVertex extends AbstractVertex {
//		private Vector normal;
//		private TextureCoordinate coords;
//
//		/**
//		 * Constructor.
//		 * @param pos Vertex position
//		 */
//		public MutableVertex(Point pos) {
//			super(pos);
//		}
//
//		@Override
//		public Vector normal() {
//			return normal;
//		}
//
//		@Override
//		public void normal(Vector normal) {
//			this.normal = notNull(normal);
//		}
//
//		@Override
//		public TextureCoordinate coordinates() {
//			return coords;
//		}
//
//		/**
//		 * Sets the texture coordinates.
//		 * @param coords Coordinates
//		 */
//		public void coordinates(TextureCoordinate coords) {
//			this.coords = notNull(coords);
//		}
//
//		@Override
//		public void buffer(FloatBuffer buffer) {
//			pos.buffer(buffer);
//			normal.buffer(buffer);
//			coords.buffer(buffer);
//		}
//	}
//
//	/**
//	 * @return Vertex position
//	 */
//	Point position();
//
//	/**
//	 * @return Vertex normal
//	 */
//	Vector normal();
//
//	/**
//	 * Sets the vertex normal
//	 * @return Vertex normal
//	 */
//	void normal(Vector normal);
//
//	/**
//	 * @return Vertex texture coordinates
//	 */
//	TextureCoordinate coordinates();
//}
