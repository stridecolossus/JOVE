package org.sarge.jove.model;

import static org.sarge.jove.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;

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
	 * Creates a simple model.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @return New model
	 * @throws IllegalArgumentException if any vertex does not match the given layout
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @see Primitive#isValidVertexCount(int)
	 */
	static Model of(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices) {
		final var builder = new Model.Builder<>().primitive(primitive).layout(layout);
		vertices.forEach(builder::add);
		return builder.build();
	}

	/**
	 * Partial implementation.
	 */
	abstract class AbstractModel implements Model {
		private final Primitive primitive;
		private final Vertex.Layout layout;
		private final int count;

		/**
		 * Constructor.
		 * @param primitive		Drawing primitive
		 * @param layout		Vertex layout
		 * @param count			Number of vertices
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected AbstractModel(Primitive primitive, Vertex.Layout layout, int count) {
			this.primitive = notNull(primitive);
			this.layout = notNull(layout);
			this.count = count;
			validate();
		}

		/**
		 * Validates this model against the primitive.
		 */
		private void validate() {
			if(!primitive.isValidVertexCount(count)) {
				throw new IllegalArgumentException(String.format("Invalid number of vertices for primitive: count=%d primitive=%s", count, primitive));
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
		public final int count() {
			return count;
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
		protected final List<Vertex> vertices;

		/**
		 * Constructor.
		 * @param primitive		Drawing primitive
		 * @param layout		Vertex layout
		 * @param vertices		Vertices
		 * @param count			Number of vertices
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
		 * @see Primitive#isValidVertexCount(int)
		 */
		protected DefaultModel(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices, int count) {
			super(primitive, layout, count);
			this.vertices = notNull(vertices);
		}

		@Override
		public ByteBuffer vertices() {
			// Allocate buffer
			final Vertex.Layout layout = this.layout();
			final int len = vertices.size() * layout.size() * Float.BYTES;
			final ByteBuffer buffer = Bufferable.allocate(len);

			// Buffer vertices
			for(Vertex v : vertices) {
				layout.buffer(v, buffer);
			}

			// Prepare buffer
			return buffer.rewind(); // TODO - asReadOnlyBuffer(); means not equal!
		}

		@Override
		public Optional<ByteBuffer> index() {
			return Optional.empty();
		}

//		public Model buffer() {
//			return new BufferedModel(this);
//		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("vertices", vertices.size())
					.build();
		}
	}

	/**
	 * Builder for a model.
	 */
	class Builder<T extends Builder<T>> {
		protected Primitive primitive = Primitive.TRIANGLE_STRIP;
		protected Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
		protected final List<Vertex> vertices = new ArrayList<>();

		private boolean validate = true;

		@SuppressWarnings("unchecked")
		private T instance() {
			return (T) this;
		}

		/**
		 * Sets the drawing primitive for this model.
		 * @param primitive Drawing primitive
		 */
		public T primitive(Primitive primitive) {
			this.primitive = notNull(primitive);
			return instance();
		}

		/**
		 * Sets the layout for vertices in this model.
		 * @param layout Vertex layout
		 */
		public T layout(Vertex.Layout layout) {
			this.layout = notNull(layout);
			return instance();
		}

		/**
		 * Sets the layout for vertices in this model.
		 * @param layout Vertex layout
		 */
		public T layout(Vertex.Component... layout) {
			this.layout = new Vertex.Layout(layout);
			return instance();
		}

		/**
		 * Sets whether to validate vertices against the current layout (default is {@code true}).
		 * @param check Whether to validate vertices
		 */
		public final T validate(boolean validate) {
			this.validate = validate;
			return instance();
		}

		/**
		 * Adds a vertex to this model.
		 * @param v Vertex
		 * @throws IllegalArgumentException if the vertex component does not match the model layout
		 * @see Vertex.Layout#matches(Vertex)
		 * @see #validate(boolean)
		 */
		public T add(Vertex vertex) {
			if(validate && !layout.matches(vertex)) {
				throw new IllegalArgumentException(String.format("Vertex does not match the model layout: vertex=%s layout=%s", vertex, layout));
			}
			vertices.add(vertex);
			return instance();
		}

		/**
		 * Constructs this model.
		 * @return New model
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 */
		public Model build() {
			return new DefaultModel(primitive, layout, vertices, vertices.size());
		}
	}
}
