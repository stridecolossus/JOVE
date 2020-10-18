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
 * A <i>model</i> is comprised of a list of vertices and an optional index rendered according to a {@link Primitive} and {@link Vertex.Layout}.
 * @author Sarge
 */
public interface Model {
	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Vertex layout
	 */
	Vertex.Layout layout();

	/**
	 * @return Number of vertices in this model
	 */
	int count();

	/**
	 * @return Vertex buffer
	 */
	ByteBuffer vertices();

	/**
	 * @return Index buffer
	 */
	Optional<ByteBuffer> index();

	/**
	 * Creates a un-indexed model.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @return New model
	 * @throws IllegalArgumentException if any vertex does not match the given layout
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @see Primitive#isValidVertexCount(int)
	 */
	static Model of(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices) {
		final var builder = new Model.Builder().primitive(primitive).layout(layout);
		vertices.forEach(builder::add);
		return builder.build();
	}

	/**
	 * Partial implementation.
	 */
	abstract class AbstractModel implements Model {
		private final Primitive primitive;
		private final Vertex.Layout layout;

		/**
		 * Constructor.
		 * @param primitive		Drawing primitive
		 * @param layout		Vertex layout
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected AbstractModel(Primitive primitive, Vertex.Layout layout) {
			this.primitive = notNull(primitive);
			this.layout = notNull(layout);
		}

		/**
		 * Validates this model against the primitive.
		 */
		protected void validate() {
			if(!primitive.isValidVertexCount(count())) {
				throw new IllegalArgumentException(String.format("Invalid number of vertices for primitive: count=%d primitive=%s", count(), primitive));
			}
			if(layout.components().contains(Vertex.Component.NORMAL) && !primitive.hasNormals()) {
				throw new IllegalArgumentException("Drawing primitive does not support normals: " + primitive);
			}
		}

		@Override
		public final Primitive primitive() {
			return primitive;
		}

		@Override
		public final Vertex.Layout layout() {
			return layout;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}

			return
					(obj instanceof Model that) &&
					this.primitive().equals(that.primitive()) &&
					this.layout().equals(that.layout()) &&
					this.vertices().equals(that.vertices()) &&
					this.index().equals(that.index());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("primitive", primitive())
					.append("layout", layout())
					.build();
		}
	}

	/**
	 * Default implementation.
	 */
	class DefaultModel extends AbstractModel {
		private final List<Vertex> vertices;
		private final List<Integer> index;

		private ByteBuffer vertexBuffer, indexBuffer;

		/**
		 * Constructor.
		 * @param primitive		Drawing primitive
		 * @param layout		Vertex layout
		 * @param vertices		Vertices
		 * @param index			Optional index
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected DefaultModel(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices, List<Integer> index) {
			super(primitive, layout);
			this.vertices = notNull(vertices);
			this.index = index;
			validate();
		}

		@Override
		public int count() {
			if(index == null) {
				return vertices.size();
			}
			else {
				return index.size();
			}
		}

		@Override
		public ByteBuffer vertices() {
			if(vertexBuffer == null) {
				// Allocate buffer
				final Vertex.Layout layout = this.layout();
				final int len = vertices.size() * layout.size() * Float.BYTES;
				vertexBuffer = Bufferable.allocate(len);

				// Buffer vertices
				for(Vertex v : vertices) {
					layout.buffer(v, vertexBuffer);
				}
			}

			// Prepare VBO
			// TODO - asReadOnly() causes this to fail and not-equal!
			return vertexBuffer.rewind();
		}

		@Override
		public Optional<ByteBuffer> index() {
			// Check whether indexed
			if(index == null) {
				return Optional.empty();
			}

			// Build IBO
			if(indexBuffer == null) {
				final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
				indexBuffer = Bufferable.allocate(array.length * Integer.BYTES);
				indexBuffer.asIntBuffer().put(array);
			}

			// Prepare index
			return Optional.of(indexBuffer.rewind());
		}

//		public Model buffer() {
//			return new BufferedModel(this);
//		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("vertices", vertices.size())
					.append("index", index == null ? 0 : index.size())
					.build();
		}
	}

	/**
	 * Builder for a model.
	 * <p>
	 * Usage:
	 * <pre>
	 *  Model model = new Builder()
	 *  	.primitive(Primitive.TRIANGLES)
	 *  	.layout(Vertex.Component.POSITION)
	 *  	.add(vertex)
	 *  	.add(vertex)
	 *  	.add(vertex)
	 *  	.build()
	 * </pre>
	 */
	class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
		private final List<Vertex> vertices = new ArrayList<>();

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
		 * Sets the layout for vertices in this model.
		 * @param layout Vertex layout
		 */
		public Builder layout(Vertex.Component... layout) {
			this.layout = new Vertex.Layout(layout);
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
			if(!layout.matches(vertex)) {
				throw new IllegalArgumentException(String.format("Vertex does not match the model layout: vertex=%s layout=%s", vertex, layout));
			}
			vertices.add(vertex);
			return this;
		}

		/**
		 * @return Number of vertices
		 */
		protected final int count() {
			return vertices.size();
		}

		/**
		 * Adds an index.
		 * @param index Index
		 * @throws IndexOutOfBoundsException if the index is out-of-bounds for this model
		 * @throws UnsupportedOperationException for an non-indexed builder
		 */
		public Builder add(int index) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Looks up the index of the given vertex.
		 * @param vertex Vertex
		 * @return Vertex index
		 * @throws IllegalArgumentException if the vertex is not present in this model
		 * @throws UnsupportedOperationException for an non-indexed builder
		 */
		public int indexOf(Vertex vertex) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @return Index or {@code null} if not indexed
		 */
		protected List<Integer> index() {
			return null;
		}

		/**
		 * Constructs this model.
		 * @return New model
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 */
		public Model build() {
			return new DefaultModel(primitive, layout, vertices, index());
		}
	}

	/**
	 * Builder for an indexed model.
	 * <p>
	 * This implementation also performs vertex de-duplication in the {@link #add(Vertex)} method.
	 * <p>
	 * Usage:
	 * <pre>
	 *  // Ignore duplicates
	 *  IndexedBuilder builder = new IndexedModel()
	 *  	.primitive(Primitive.LINES)
	 *  	.add(vertex)
	 *  	.add(vertex);		// Duplicate ignored
	 *
	 *  // Add explicit index
	 *  builder.add(0);
	 *
	 *  // Lookup index
	 *  int index = builder.indexOf(vertex);
	 *  builder.add(index);
	 * </pre>
	 */
	public static class IndexedBuilder extends Builder {
		private final List<Integer> index = new ArrayList<>();
		private final Map<Vertex, Integer> map = new HashMap<>();

		@Override
		protected List<Integer> index() {
			return index;
		}

		@Override
		public Builder add(Vertex vertex) {
			final Integer prev = map.get(vertex);
			if(prev == null) {
				map.put(vertex, count());
				super.add(vertex);
			}
			return this;
		}

		@Override
		public Builder add(int index) {
			Check.zeroOrMore(index);
			if(index >= count()) throw new IndexOutOfBoundsException(String.format("Invalid vertex index: index=%d vertices=%s", index, count()));
			this.index.add(index);
			return this;
		}

		@Override
		public int indexOf(Vertex vertex) {
			final Integer index = map.get(vertex);
			if(index == null) throw new IllegalArgumentException("Vertex not present: " + vertex);
			return index;
		}
	}
}
