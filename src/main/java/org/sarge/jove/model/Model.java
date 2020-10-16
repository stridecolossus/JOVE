package org.sarge.jove.model;

import static org.sarge.jove.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.Check;

/**
 * A <i>model</i> is comprised of a list of vertices that can be rendered according to a {@link Primitive} and {@link Vertex.Layout}.
 * @author Sarge
 */
public class Model {
	/**
	 * Creates a model.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @return New model
	 * @throws IllegalArgumentException if any vertex does not match the given layout
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @see Primitive#isValidVertexCount(int)
	 */
	public static Model of(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices) {
		final var builder = new Model.Builder().primitive(primitive).layout(layout);
		vertices.forEach(builder::add);
		return builder.build();
	}

	private final Primitive primitive;
	private final Vertex.Layout layout;
	private final List<Vertex> vertices;
	private final int[] index;

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @param index			Optional index buffer
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
	 * @see Primitive#isValidVertexCount(int)
	 */
	private Model(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices, int[] index) {
		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		this.vertices = notNull(vertices);
		this.index = index;
		validate();
	}

	private void validate() {
		final int size = index == null ? vertices.size() : index.length;
		if(!primitive.isValidVertexCount(size)) {
			throw new IllegalArgumentException(String.format("Invalid number of vertices for primitive: size=%d primitive=%s", size, primitive));
		}
		if(layout.components().contains(Vertex.Component.NORMAL) && !primitive.hasNormals()) {
			throw new IllegalArgumentException("Drawing primitive does not support normals: " + primitive);
		}
	}

	/**
	 * @return Drawing primitive
	 */
	public Primitive primitive() {
		return primitive;
	}

	/**
	 * @return Vertex layout
	 */
	public Vertex.Layout layout() {
		return layout;
	}

	/**
	 * @return Number of vertices in this model
	 */
	public int size() {
		if(index == null) {
			return vertices.size();
		}
		else {
			return index.length;
		}
	}

	/**
	 * @return Index buffer
	 */
	public Optional<Bufferable> index() {
		// Ignore if no index
		if(index == null) {
			return Optional.empty();
		}

		// Otherwise create index buffer
		final Bufferable buffer = new Bufferable() {
			@Override
			public long length() {
				return index.length * Integer.BYTES;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				buffer.asIntBuffer().put(index);
			}
		};
		return Optional.of(buffer);
	}

	/**
	 * @return Interleaved vertex buffer
	 */
	public Bufferable vertices() {
		return new Bufferable() {
			@Override
			public long length() {
				return  vertices.size() * layout.size() * Float.BYTES;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Vertex v : vertices) {
					for(Vertex.Component c : layout.components()) {
						c.map(v).buffer(buffer);
					}
				}
			}
		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("primitive", primitive)
				.append("layout", layout)
				.append("vertices", vertices.size())
				.append("index", index == null ? 0 : index.length)
				.build();
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
		private final List<Vertex> vertices = new ArrayList<>();
		private boolean validate = true;

		/**
		 * @return Number of vertices
		 */
		protected final int count() {
			return vertices.size();
		}

		/**
		 * Sets the drawing primitive for this model.
		 * @param primitive Drawing primitive
		 */
		public Builder primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			return this;
		}

		/**
		 * Sets the layout for vertices in this model.
		 * @param layout Vertex layout
		 */
		public Builder layout(Vertex.Layout layout) {
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Sets whether to validate vertices against the current layout (default is {@code true}).
		 * @param check Whether to validate vertices
		 */
		public Builder validate(boolean validate) {
			this.validate = validate;
			return this;
		}

		/**
		 * Adds a vertex to this model.
		 * @param v Vertex
		 * @throws IllegalArgumentException if the vertex component does not match the model layout
		 * @see Vertex.Layout#matches(Vertex)
		 * @see #validate(boolean)
		 */
		public Builder add(Vertex vertex) {
			if(validate && !layout.matches(vertex)) {
				throw new IllegalArgumentException(String.format("Vertex does not match the model layout: vertex=%s layout=%s", vertex, layout));
			}
			vertices.add(vertex);
			return this;
		}

		/**
		 * Looks up the index of the given vertex.
		 * @param vertex Vertex
		 * @return Index
		 * @throws UnsupportedOperationException by default
		 * @throws IllegalArgumentException if the vertex is not present in this model
		 */
		public int indexOf(Vertex vertex) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Adds an index.
		 * @param index Vertex index
		 * @throws UnsupportedOperationException by default
		 * @throws IllegalArgumentException if the index is out-of-bounds for this model
		 */
		public Builder add(int index) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @return Index
		 */
		protected int[] index() {
			return null;
		}

		/**
		 * Constructs this model.
		 * @return New model
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 */
		public Model build() {
			return new Model(primitive, layout, vertices, index());
		}
	}

	/**
	 * Builder for an indexed model that performs vertex de-duplication.
	 */
	public static class IndexedBuilder extends Builder {
		private final Map<Vertex, Integer> map = new HashMap<>();
		private final List<Integer> index = new ArrayList<>();

		@Override
		public IndexedBuilder add(int index) {
			Check.zeroOrMore(index);
			if(index >= count()) throw new IllegalArgumentException(String.format("Invalid vertex index: index=%d vertices=%s", index, count()));
			this.index.add(index);
			return this;
		}

		@Override
		public IndexedBuilder add(Vertex vertex) {
			final Integer prev = map.get(vertex);
			if(prev == null) {
				// Add new vertex
				final int idx = count();
				super.add(vertex);

				// Register indexed vertex
				add(idx);
				map.put(vertex, idx);
			}
			else {
				// Otherwise use existing index
				add(prev);
			}
			return this;
		}

		@Override
		public int indexOf(Vertex vertex) {
			final Integer index = map.get(vertex);
			if(index == null) throw new IllegalArgumentException("Vertex not present: " + vertex);
			return index;
		}

		@Override
		protected int[] index() {
			return index.stream().mapToInt(Integer::intValue).toArray();
		}
	}
}
