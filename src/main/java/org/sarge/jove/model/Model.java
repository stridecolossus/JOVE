package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Mask;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is a mutable representation of a renderable object comprising vertices and an optional index.
 * <p>
 * The structure of the model vertices is specified by the {@link #layout(Component)}.
 * TODO - does not check?
 * <p>
 * A model can optionally be indexed using {@link #add(int)}.
 * The data type of the index is {@code short} is the index is small and {@link #compact(boolean)} is set, otherwise it is comprised of {@code int} values.
 * <p>
 * The {@link #mesh()} method is used to generate the renderable mesh.
 * Note that changes to the model are reflected in the resultant mesh.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create model for a triangle with vertex positions and normals
 * Model model = new Model(Primitive.TRIANGLES);
 * model.layout(Point.LAYOUT);
 * model.layout(Model.NORMALS);
 *
 * // Add vertices
 * model.add(Vertex.of(new Point(...)));
 * ...
 *
 * // Add index
 * model.add(0);
 * model.add(1);
 * ...
 *
 * // Generate mesh and bounds
 * Mesh mesh = model.mesh();
 * Bounds bounds = model.bounds();
 * </pre>
 * <p>
 * @author Sarge
 */
public class Model {
	/**
	 * Vertex normal layout.
	 */
	public static final Component NORMALS = Component.floats(3);

	/**
	 * Size of a {@code short} index.
	 */
	private static final long SHORT_INDEX = Mask.unsignedMaximum(Short.SIZE);

	/**
	 * Determines whether the given draw count requires an {@code int} index.
	 * @param count Draw count
	 * @return Whether the index data type is integral
	 */
	public static boolean isIntegerIndex(int count) {
		return count >= SHORT_INDEX;
	}

	/**
	 * A <i>model header</i> specifies the structure of a model.
	 * @author Sarge
	 */
	public interface Header {
		/**
		 * @return Drawing primitive
		 */
		Primitive primitive();

		/**
		 * @return Draw count
		 */
		int count();

		/**
		 * @return Vertex layout
		 */
		Layout layout();

		/**
		 * @return Whether this model has an index
		 */
		boolean isIndexed();
	}

	/**
	 * Header for this model.
	 */
	private class DefaultHeader extends AbstractModelHeader {
		private final List<Component> components = new ArrayList<>();

		private DefaultHeader(Primitive primitive) {
			super(primitive);
		}

		@Override
		public int count() {
			if(isIndexed()) {
				return index.size() - restart;
			}
			else {
				return vertices.size();
			}
		}

		@Override
		public Layout layout() {
			return new Layout(components);
		}

		@Override
		public boolean isIndexed() {
			return !index.isEmpty();
		}
	}

	private final DefaultHeader header;
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Integer> index = new ArrayList<>();
	private int restart;
	private boolean compact = true;

	/**
	 * Constructor.
	 * @param primitive Drawing primitive
	 */
	public Model(Primitive primitive) {
		header = new DefaultHeader(primitive);
	}

	/**
	 * @return Header for this model
	 */
	public Header header() {
		return header;
	}

	/**
	 * Adds a layout component to this model.
	 * @param c Layout component
	 * @throws IllegalArgumentException if the component is {@link #NORMALS} but the drawing {@link #primitive()} does not support normals
	 * @see Primitive#isNormalSupported()
	 */
	public Model layout(Component c) {
		if((c == NORMALS) && !header.primitive.isNormalSupported()) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + this);
		}
		header.components.add(c);
		return this;
	}

	/**
	 * @return Model vertices
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex to this model.
	 * @param v Vertex to add
	 */
	public Model add(Vertex v) {
		Check.notNull(v);
		vertices.add(v);
		return this;
	}

	/**
	 * @return Model index
	 */
	public IntStream index() {
		return index.stream().mapToInt(Integer::intValue);
	}

	/**
	 * Adds a vertex index to this model.
	 * @param index Vertex index
	 * @throws IndexOutOfBoundsException if {@link #index} is not a valid vertex index
	 */
	public Model add(int index) {
		if((index < 0) || (index >= vertices.size())) throw new IndexOutOfBoundsException(index);
		this.index.add(index);
		return this;
	}

	/**
	 * Restarts the index.
	 * @throws IllegalStateException if this model is not {@link #isIndexed()}
	 */
	public Model restart() {
		if(!header.isIndexed()) throw new IllegalStateException("Cannot restart an unindexed model");
		index.add(-1);
		++restart;
		return this;
	}

	/**
	 * Sets whether the index buffer uses the most <i>compact</i> data type (default is {@code true}).
	 * <p>
	 * If {@link #compact} is set, the data type of the index buffer is {@code short} if the index is small enough.
	 * Otherwise the index is comprised of {@code int} values.
	 * TODO - revise doc, restart precludes, note still stored as integers
	 * <p>
	 * @param compact Whether to use compact indices
	 * @see #isIntegerIndex(int)
	 */
	public Model compact(boolean compact) {
		this.compact = compact;
		return this;
	}

	/**
	 * Mesh vertex buffer.
	 */
	private class VertexBuffer implements Bufferable {
		@Override
		public int length() {
			final Layout layout = new Layout(header.components);
			return vertices.size() * layout.stride();
		}

		@Override
		public void buffer(ByteBuffer bb) {
			for(Vertex v : vertices) {
				v.buffer(bb);
			}
		}
	}

	/**
	 * Mesh index buffer.
	 */
	private class IndexBuffer implements Bufferable {
		/**
		 * @return Whether the index requires a {@code int} type
		 */
		private boolean isIntegral() {
			if(compact) {
				return (restart > 0) || isIntegerIndex(index.size());
			}
			else {
				return true;
			}
		}
		// TODO - move compact to parameter of mesh factory?

		@Override
		public int length() {
			final int bytes = isIntegral() ? Integer.BYTES : Short.BYTES;
			return index.size() * bytes;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			if(isIntegral())  {
				if(bb.isDirect()) {
					for(int n : index) {
						bb.putInt(n);
					}
				}
				else {
					final int[] indices = index().toArray();
					bb.asIntBuffer().put(indices);
// TODO - does not update the position!!!
//					bb.position(bb.position() + indices.length * Integer.BYTES);
				}
			}
			else {
				for(int n : index) {
					bb.putShort((short) n);
				}
			}
		}
	}

	/**
	 * Creates a buffered instance of this model.
	 * @return This buffered model
	 */
	public BufferedModel buffer() {
		return new BufferedModel() {
			@Override
			public Header header() {
				return header;
			}

			@Override
			public Bufferable vertices() {
				validate();
				return new VertexBuffer();
			}

			/**
			 * @return Index
			 */
			@Override
			public Optional<Bufferable> index() {
				validate();
				if(header.isIndexed()) {
					return Optional.of(new IndexBuffer());
				}
				else {
					return Optional.empty();
				}
			}
		};
	}

	/**
	 * Calculates the bounds of this model.
	 * @return Model bounds
	 * @throws IllegalStateException if the model layout does not contain a {@link Point#LAYOUT} component
	 * @throws ArrayIndexOutOfBoundsException if any vertex does not contain a vertex position
	 */
	public Bounds bounds() {
		// Determine vertex position from layout
		final int pos = header.components.indexOf(Point.LAYOUT);
		if(pos == -1) throw new IllegalStateException("Model layout does not contain a vertex position: " + this);

		// Construct bounds
		final var bounds = new Bounds.Builder();
		for(Vertex v : vertices) {
			final Point p = v.component(pos);
			bounds.add(p);
		}
		return bounds.build();
	}

	/**
	 * @throws IllegalStateException if the model cannot be rendered
	 */
	private void validate() {
		if(header.components.isEmpty()) throw new IllegalStateException("Undefined model layout: " + this);
		if(!header.primitive.isValidVertexCount(header.count())) throw new IllegalStateException("Invalid draw count for primitive: " + this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(header)
				.append("vertices", vertices.size())
				.build();
	}
}
