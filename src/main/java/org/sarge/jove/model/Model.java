package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.platform.vulkan.VkFrontFace;

/**
 * A <i>model</i> is comprised of a list of vertices and an optional index rendered according to a {@link Primitive} and {@link Vertex.Layout}.
 * @author Sarge
 */
public interface Model {
	/**
	 * Default model name.
	 */
	String DEFAULT_NAME = "model";

	/**
	 * @return Model name
	 */
	String name();

	/**
	 * @return Drawing primitive
	 */
	Primitive primitive();

	/**
	 * @return Winding order for front-facing polygons
	 */
	VkFrontFace winding();

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
	 * Partial implementation.
	 */
	abstract class AbstractModel implements Model {
		private final String name;
		private final Primitive primitive;
		private final VkFrontFace winding;
		private final Vertex.Layout layout;

		/**
		 * Constructor.
		 * @param name			Model name
		 * @param primitive		Drawing primitive
		 * @param winding		Winding order for front-facing polygons
		 * @param layout		Vertex layout
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected AbstractModel(String name, Primitive primitive, VkFrontFace winding, Vertex.Layout layout) {
			this.name = notEmpty(name);
			this.primitive = notNull(primitive);
			this.winding = notNull(winding);
			this.layout = notNull(layout);
		}

		/**
		 * Validates this model against the primitive.
		 */
		protected void validate() {
			if(count() == 0) {
				throw new IllegalArgumentException("Empty model");
			}

			if(!primitive.isValidVertexCount(count())) {
				throw new IllegalArgumentException(String.format("Invalid number of vertices for primitive: count=%d primitive=%s", count(), primitive));
			}

			if(layout.components().contains(Vertex.Component.NORMAL) && !primitive.hasNormals()) {
				throw new IllegalArgumentException("Drawing primitive does not support normals: " + primitive);
			}
		}

		/**
		 * @return Model name
		 */
		@Override
		public final String name() {
			return name;
		}

		@Override
		public final Primitive primitive() {
			return primitive;
		}

		@Override
		public final VkFrontFace winding() {
			return winding;
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
					this.winding().equals(that.winding()) &&
					this.layout().equals(that.layout()) &&
					this.vertices().equals(that.vertices()) &&
					this.index().equals(that.index());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("name", name)
					.append("primitive", primitive())
					.append("winding", winding())
					.append("layout", layout())
					.append("count", count())
					.append("indexed", index().isPresent())
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
		 * @param name			Model name
		 * @param primitive		Drawing primitive
		 * @param winding		Winding order for front-facing polygons
		 * @param layout		Vertex layout
		 * @param vertices		Vertices
		 * @param index			Optional index
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected DefaultModel(String name, Primitive primitive, VkFrontFace winding, Vertex.Layout layout, List<Vertex> vertices, List<Integer> index) {
			super(name, primitive, winding, layout);
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
				final int[] array = index.stream().mapToInt(Integer::intValue).toArray(); // TODO - is this the best way?
				indexBuffer = Bufferable.allocate(array.length * Integer.BYTES);
				indexBuffer.asIntBuffer().put(array);
			}

			// Prepare index
			return Optional.of(indexBuffer.rewind());
		}

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
		private String name = "model";
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private VkFrontFace winding = VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE;
		private Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
		private final List<Vertex> vertices = new ArrayList<>();

		/**
		 * Sets the name of this model.
		 * @param name Model name
		 */
		public Builder name(String name) {
			this.name = notEmpty(name);
			return this;
		}

		/**
		 * Sets the drawing primitive for this model (default is {@link Primitive#TRIANGLE_STRIP}).
		 * @param primitive Drawing primitive
		 */
		public Builder primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			return this;
		}

		/**
		 * Sets the winding order for front-facing polygons (default is counter-clockwise).
		 * @param winding Winding order
		 */
		public Builder windingOrder(VkFrontFace winding) {
			this.winding = notNull(winding);
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
			return new DefaultModel(name, primitive, winding, layout, vertices, index());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("name", name)
					.append("primitive", primitive)
					.append("layout", layout)
					.append("vertices", count())
					.build();
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
		public IndexedBuilder add(Vertex vertex) {
			// Lookup existing vertex index
			final Integer prev = map.get(vertex);

			if(prev == null) {
				// Add new vertex
				final int idx = count();
				map.put(vertex, idx);
				index.add(idx);
				super.add(vertex);
			}
			else {
				// Add existing vertex
				index.add(prev);
			}

			return this;
		}

		/**
		 * Looks up the index of the given vertex.
		 * @param vertex Vertex
		 * @return Vertex index
		 * @throws IllegalArgumentException if the vertex is not present in this model
		 * @throws UnsupportedOperationException for an non-indexed builder
		 */
		public int indexOf(Vertex vertex) {
			final Integer index = map.get(vertex);
			if(index == null) throw new IllegalArgumentException("Vertex not present: " + vertex);
			return index;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("index", index.size())
					.build();
		}
	}
}
