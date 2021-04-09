package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.Layout;

/**
 * A <i>vertex model</i> is comprised of a collection of vertices.
 * TODO
 * - buffers on demand
 * @author Sarge
 */
public class VertexModel implements Model {
	private final Header header;
	private final Layout layout;
	private final List<Vertex> vertices;
	private final Optional<List<Integer>> index;

	/**
	 * Constructor.
	 * @param header			Model header
	 * @param layout			Vertex layout
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	public VertexModel(Header header, Layout layout, List<Vertex> vertices, List<Integer> index) {
		this.header = notNull(header);
		this.layout = notNull(layout);
		this.vertices = List.copyOf(vertices);
		this.index = Optional.ofNullable(index);
	}

	@Override
	public Header header() {
		return header;
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
	}

	/**
	 * @return Vertex layout of this model
	 */
	public Layout layout() {
		return layout;
	}

	/**
	 * @return Vertices
	 */
	public List<Vertex> vertices() {
		return vertices;
	}

	/**
	 * @return Index
	 */
	public Optional<List<Integer>> index() {
		return index;
	}

	@Override
	public Bufferable vertexBuffer() {
		return new Bufferable() {
			private final int len = vertices.size() * layout.size() * Float.BYTES;

			@Override
			public long length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(final Vertex v : vertices) {
					layout.buffer(v, buffer);
				}
			}
		};
	}

	@Override
	public Optional<Bufferable> indexBuffer() {
		return index.map(VertexModel::index);
	}

	/**
	 * Creates the index buffer.
	 */
	private static Bufferable index(List<Integer> index) {
		return new Bufferable() {
			@Override
			public long length() {
				return index.size() * Integer.BYTES;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
				buffer.asIntBuffer().put(array);
			}
		};
	}

	/**
	 * Converts this model to a buffered implementation.
	 * @return Buffered model
	 */
	public BufferedModel buffer() {
		return new BufferedModel(header, vertexBuffer(), indexBuffer().orElse(null));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(header)
				.append("vertices", vertices.size())
				.append("index", index.map(List::size).orElse(0))
				.build();
	}

	/**
	 * Builder for a vertex model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private boolean clockwise;
		private Layout layout = new Layout(Component.POSITION);
		protected final List<Vertex> vertices = new ArrayList<>();

		/**
		 * Sets the drawing primitive (default is {@link Primitive#TRIANGLE_STRIP}).
		 * @param primitive Drawing primitive
		 */
		public Builder primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			return this;
		}

		/**
		 * Sets the triangle winding order (default is {@code false}, counter-clockwise).
		 * @param clockwise Triangle winding order
		 */
		public Builder clockwise(boolean clockwise) {
			this.clockwise = clockwise;
			return this;
		}

		/**
		 * Sets the vertex layout.
		 * TODO - undefined if changed with vertices added? ditto primitive?
		 * @param layout Vertex layout
		 */
		public Builder layout(Layout layout) {
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Adds a vertex.
		 * @param v Vertex
		 * @throws IllegalArgumentException if the vertex does not match the configured layout
		 */
		public Builder add(Vertex v) {
			if(!layout.matches(v)) {
				throw new IllegalArgumentException(String.format("Vertex %s does not match layout %s", v, layout));
			}
			vertices.add(v);
			return this;
		}

		/**
		 * Constructs this model.
		 * @return New vertex model
		 */
		public VertexModel build() {
			return build(vertices.size(), null);
		}

		/**
		 * Constructs this model.
		 * @param count		Vertex count
		 * @param index		Optional index
		 * @return New model
		 * @throws IllegalArgumentException for an invalid model
		 */
		protected VertexModel build(int count, List<Integer> index) {
			final Header header = new Header(primitive, clockwise, count);
			return new VertexModel(header, layout, vertices, index);
		}
	}

	/**
	 * Builder for an indexed model.
	 */
	public static class IndexedBuilder extends Builder {
		private final List<Integer> index = new ArrayList<>();
		private final Map<Vertex, Integer> map = new HashMap<>();

		@Override
		public Builder add(Vertex v) {
			final Integer prev = map.get(v);
			if(prev == null) {
				// Add new vertex and register index
				final Integer n = vertices.size();
				index.add(n);
				map.put(v, n);
				super.add(v);
			}
			else {
				// Otherwise add index for existing vertex
				index.add(prev);
			}
			return this;
		}

		@Override
		public VertexModel build() {
			return build(index.size(), index);
		}
	}
}
