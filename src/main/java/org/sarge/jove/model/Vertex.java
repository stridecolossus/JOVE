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
 * A <i>vertex</i> is comprised of {@link Bufferable} data.
 * <p>
 * Note that the order that components are written using {@link #buffer(FloatBuffer)} is assumed to be dependant on the implementing vertex class.
 * It is the responsibility of the user to ensure that the location of components within a vertex matches the target shader.
 *
 * For example, the convenience {@link MutableVertex} class could be extended to omit components or over-ride the buffering order:
 * <pre>
 * class CustomVertex extends MutableVertex {
 *     public int size() {
 *         return Coordinate2D.SIZE + Point.SIZE;
 *     }
 *
 *     public void buffer(FloatBuffer fb) {
 *         coords.buffer(fb);
 *         pos.buffer(fb);
 *     }
 * }
 * </pre>
 *
 *
 * TODO - revert to vertex returning components[]? has to return size anyway
 *
 * @author Sarge
 */
public interface Vertex extends Bufferable {
	/**
	 * Vertex component descriptor.
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
	 * A <i>mutable normal vertex</i> defines a vertex with a normal that can be accumulated.
	 */
	interface MutableNormalVertex extends Vertex {
		/**
		 * @return Vertex position
		 */
		Point position();

		/**
		 * @return Vertex normal
		 */
		Vector normal();

		/**
		 * Sets the vertex normal.
		 * @param normal Normal
		 */
		void normal(Vector normal);
	}

	/**
	 * A <i>mutable vertex</i> is a default implementation used to construct a model consisting of a vertex position, normal and 2D texture-coordinate components.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>the default normal is undefined</li>
	 * <li><b>all</b> components are written by the {@link #buffer(FloatBuffer)}</li>
	 * <li>it is the responsibility of the user to select the required components, see {@link Vertex}</li>
	 * </ul>
	 */
	class MutableVertex extends AbstractEqualsObject implements MutableNormalVertex {
		/**
		 * Size of a mutable vertex.
		 */
		public static final int SIZE = Component.size(List.of(Component.POSITION, Component.NORMAL, Component.coordinate(2)));

		private static final Vector EMPTY = new Vector(0, 0, 0);

		protected Point pos;
		protected Vector normal = EMPTY;
		protected TextureCoordinate.Coordinate2D coords = TextureCoordinate.Coordinate2D.Corner.BOTTOM_LEFT.coordinates();

		/**
		 * Default constructor.
		 */
		public MutableVertex() {
			this(Point.ORIGIN);
		}

		/**
		 * Constructor.
		 * @param pos Vertex position
		 */
		public MutableVertex(Point pos) {
			position(pos);
		}

		@Override
		public Point position() {
			return pos;
		}

		/**
		 * Sets the position of this vertex.
		 * @param pos Vertex position
		 */
		public void position(Point pos) {
			this.pos = notNull(pos);
		}

		@Override
		public Vector normal() {
			return normal;
		}

		@Override
		public void normal(Vector normal) {
			this.normal = notNull(normal);
		}

		/**
		 * @return Texture coordinates
		 */
		public TextureCoordinate coordinates() {
			return coords;
		}

		/**
		 * Sets the texture coordinates.
		 * @param coords Coordinates
		 */
		public void coordinates(TextureCoordinate.Coordinate2D coords) {
			this.coords = notNull(coords);
		}

		@Override
		public int size() {
			return SIZE;
		}

		@Override
		public void buffer(FloatBuffer buffer) {
			pos.buffer(buffer);
			normal.buffer(buffer);
			coords.buffer(buffer);
		}
	}
}
