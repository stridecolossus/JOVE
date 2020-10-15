package org.sarge.jove.model;

import static org.sarge.jove.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;

/**
 * A <i>model</i> is comprised of a list of vertices that can be rendered according to a {@link Primitive} and {@link Vertex.Layout}.
 * @author Sarge
 */
public class Model implements Bufferable {
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

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @see Primitive#isValidVertexCount(int)
	 */
	private Model(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices) {
		if(!primitive.isValidVertexCount(vertices.size())) {
			throw new IllegalArgumentException(String.format("Invalid number of vertices for primitive: size=%d primitive=%s", vertices.size(), primitive));
		}

		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		this.vertices = notNull(vertices);
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
		return vertices.size();
	}

	@Override
	public long length() {
		return vertices.size() * layout.size() * Float.BYTES;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Vertex v : vertices) {
			for(Vertex.Component c : layout.components()) {
				c.map(v).buffer(buffer);
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("primitive", primitive)
				.append("layout", layout)
				.append("vertices", vertices.size())
				.build();
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
		private final List<Vertex> vertices = new ArrayList<>();

		/**
		 * @throws IllegalStateException if any vertices have already been added
		 */
		private void check() {
			if(!vertices.isEmpty()) throw new IllegalStateException("Cannot modify existing model");
		}

		/**
		 * Sets the drawing primitive for this model.
		 * @param primitive Drawing primitive
		 * @throws IllegalStateException if any vertices have already been added
		 */
		public Builder primitive(Primitive primitive) {
			check();
			this.primitive = notNull(primitive);
			return this;
		}

		/**
		 * Sets the layout for vertices in this model.
		 * @param layout Vertex layout
		 * @throws IllegalStateException if any vertices have already been added
		 */
		public Builder layout(Vertex.Layout layout) {
			check();
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Adds a vertex to this model.
		 * @param v Vertex
		 * @throws IllegalArgumentException if the vertex component do not match the model layout
		 * @see Vertex.Layout#matches(Vertex)
		 */
		public Builder add(Vertex vertex) {
			if(!layout.matches(vertex)) {
				throw new IllegalArgumentException(String.format("Vertex does not match the model layout: vertex=%s layout=%s", vertex, layout));
			}
			// TODO - clone vertex?
			vertices.add(vertex);
			return this;
		}

		/**
		 * Constructs this model.
		 * @return New model
		 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
		 * @see Primitive#isValidVertexCount(int)
		 */
		public Model build() {
			return new Model(primitive, layout, vertices);
		}
	}
}
