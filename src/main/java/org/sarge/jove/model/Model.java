package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.util.Mask;
import org.sarge.lib.util.Check;

/**
 * A <i>model</i> is a renderable object comprised vertex data and an optional index.
 * @author Sarge
 */
public class Model implements Header {
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

	private final Primitive primitive;
	private final List<Component> components = new ArrayList<>();
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Integer> index = new ArrayList<>();
	private int restart;
	private boolean compact = true;

	/**
	 * Constructor.
	 * @param primitive Drawing primitive
	 */
	public Model(Primitive primitive) {
		this.primitive = notNull(primitive);
	}

	@Override
	public Primitive primitive() {
		return primitive;
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

	/**
	 * Adds a layout component to this model.
	 * @param c Layout component
	 * @throws IllegalArgumentException if the component is {@link #NORMALS} but the drawing {@link #primitive()} does not support normals
	 * @see Primitive#isNormalSupported()
	 */
	public Model layout(Component c) {
		if((c == NORMALS) && !primitive.isNormalSupported()) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + this);
		}
		components.add(c);
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

	@Override
	public boolean isIndexed() {
		return !index.isEmpty();
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
		if(!isIndexed()) throw new IllegalStateException("Cannot restart an unindexed model");
		index.add(-1);
		++restart;
		return this;
	}

	/**
	 * Sets whether the index buffer uses the most <i>compact</i> data type (default is {@code true}).
	 * <p>
	 * If {@link #compact} is set, the data type of the index buffer is {@code short} if the index is small enough.
	 * Otherwise the index is comprised of {@code int} values.
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
			final Layout layout = new Layout(components);
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

		@Override
		public int length() {
			final int bytes = isIntegral() ? Integer.BYTES : Short.BYTES;
			return index.size() * bytes;
		}

		@Override
		public void buffer(ByteBuffer bb) {
			if(isIntegral())  {
//				if(bb.isDirect()) {
					for(int n : index) {
						bb.putInt(n);
					}
//				}
//				else {
//					final int[] indices = index().toArray();
//					bb.asIntBuffer().put(indices);
//				}
			}
			else {
				for(int n : index) {
					bb.putShort((short) n);
				}
			}
		}
	}

	/**
	 * Mesh implementation.
	 */
	private class DefaultMesh implements Mesh {
		@Override
		public Header header() {
			return Model.this;
		}

		@Override
		public Bufferable vertices() {
			validate();
			return new VertexBuffer();
		}

		@Override
		public Optional<Bufferable> index() {
			validate();
			if(isIndexed()) {
				return Optional.of(new IndexBuffer());
			}
			else {
				return Optional.empty();
			}
		}
	}

	/**
	 * @return This model as a renderable mesh
	 */
	public Mesh mesh() {
		return new DefaultMesh();
	}

	/**
	 * @throws IllegalStateException if the model cannot be rendered
	 */
	private void validate() {
		if(components.isEmpty()) throw new IllegalStateException("Undefined model layout: " + this);
		if(!primitive.isValidVertexCount(count())) throw new IllegalStateException("Invalid draw count for primitive: " + this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append("count", count())
				.append(components)
				.build();
	}
}
