package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.texture.TextureCoordinate;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;

/**
 * A <i>vertex</i> is comprised of a vertex position, normal and texture coordinates.
 * <p>
 * Notes:
 * <ul>
 * <li>only the vertex position is mandatory</li>
 * <li>it is the responsibility of the vertex implementation to ensure that components are output by {@link #buffer(FloatBuffer)} in the expected order</li>
 * </ul>
 * @author Sarge
 */
public interface Vertex extends Bufferable {
	/**
	 * A <i>vertex component</i> describes the structure of part of a vertex.
	 */
	class Component extends AbstractObject {
		/**
		 * Component data type.
		 */
		public enum Type {
			INT,
			FLOAT,
			NORM,
			SCALED,
			SRGB,				// TODO - required?
		}

		/**
		 * Vertex position component.
		 */
		public static final Component POSITION = new Component(Point.SIZE);

		/**
		 * Normal component.
		 */
		public static final Component NORMAL = new Component(Vector.SIZE);

		/**
		 * Colour component.
		 */
		public static final Component COLOUR = new Component(Colour.SIZE);

		/**
		 * Texture coordinate components with 1..3 dimensions.
		 */
		private static final List<Component> TEXTURE_COORDINATE = IntStream.range(1, 4).mapToObj(Component::new).collect(toList());

		/**
		 * Looks up a texture coordinate component of the given dimension.
		 * @param dim Dimension 1..3
		 * @return Texture coordinate component
		 * @throws IndexOutOfBoundsException if the dimension is not in the range 1..3
		 */
		public static Component coordinate(int dim) {
			return TEXTURE_COORDINATE.get(dim - 1);
		}

		/**
		 * Helper - Calculates the total size of the given components.
		 * @param components Components
		 * @return Total size
		 */
		public static int size(Collection<Component> components) {
			return components.stream().mapToInt(Component::size).sum();
		}

		private final Type type;
		private final boolean signed;
		private final int size;
		private final int bytes;

		/**
		 * Constructor.
		 * @param type		Data type
		 * @param signed	Whether the data type is signed
		 * @param size 		Size of this component
		 * @param bytes		Bytes per component
		 */
		public Component(Type type, boolean signed, int size, int bytes) {
			this.type = notNull(type);
			this.signed = signed;
			this.size = oneOrMore(size);
			this.bytes = oneOrMore(bytes);
		}

		/**
		 * Convenience constructor for a signed floating-point component.
		 * @param size Size of this component
		 */
		public Component(int size) {
			this(Type.FLOAT, true, size, Float.BYTES);
		}

		/**
		 * @return Data type
		 */
		public Type type() {
			return type;
		}

		/**
		 * @return Whether this component is signed
		 */
		public boolean isSigned() {
			return signed;
		}

		/**
		 * @return Size of this component
		 */
		public int size() {
			return size;
		}

		/**
		 * @return Bytes per component
		 */
		public int bytes() {
			return bytes;
		}
	}

	/**
	 * Partial implementation comprised only of a vertex position.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>the vertex normal and texture coordinates are initially <tt>null</tt></li>
	 * <li>{@link AbstractVertex#normal(Vector)} throws {@link UnsupportedOperationException} by default
	 * </ul>
	 */
	abstract class AbstractVertex extends AbstractEqualsObject implements Vertex {
		protected final Point pos;

		/**
		 * Constructor.
		 * @param pos Vertex position
		 */
		protected AbstractVertex(Point pos) {
			this.pos = notNull(pos);
		}

		@Override
		public Point position() {
			return pos;
		}

		@Override
		public Vector normal() {
			return null;
		}

		@Override
		public void normal(Vector normal) {
			throw new UnsupportedOperationException();
		}

		@Override
		public TextureCoordinate coordinates() {
			return null;
		}
	}

	/**
	 * A <i>mutable vertex</i> is a default implementation used by model builders.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>the vertex normal and texture coordinates are initially {@code null}</li>
	 * <li>the user is responsible for ensuring that {@code null} components are not passed to {@link #buffer(FloatBuffer)}</li>
	 * </ul>
	 */
	class MutableVertex extends AbstractVertex {
		private Vector normal;
		private TextureCoordinate coords;

		/**
		 * Constructor.
		 * @param pos Vertex position
		 */
		public MutableVertex(Point pos) {
			super(pos);
		}

		@Override
		public Vector normal() {
			return normal;
		}

		@Override
		public void normal(Vector normal) {
			this.normal = notNull(normal);
		}

		@Override
		public TextureCoordinate coordinates() {
			return coords;
		}

		/**
		 * Sets the texture coordinates.
		 * @param coords Coordinates
		 */
		public void coordinates(TextureCoordinate coords) {
			this.coords = notNull(coords);
		}

		@Override
		public void buffer(FloatBuffer buffer) {
			pos.buffer(buffer);
			normal.buffer(buffer);
			coords.buffer(buffer);
		}
	}

	/**
	 * @return Vertex position
	 */
	Point position();

	/**
	 * @return Vertex normal
	 */
	Vector normal();

	/**
	 * Sets the vertex normal
	 * @return Vertex normal
	 */
	void normal(Vector normal);

	/**
	 * @return Vertex texture coordinates
	 */
	TextureCoordinate coordinates();
}
