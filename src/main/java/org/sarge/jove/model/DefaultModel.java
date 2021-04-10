package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.Layout;

/**
 * A <i>default model</i> is comprised of a collection of vertices.
 * TODO
 * - buffers on demand
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices;
	private final Optional<List<Integer>> index;

	/**
	 * Constructor.
	 * @param header			Model header
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	public DefaultModel(Header header, List<Vertex> vertices, List<Integer> index) {
		super(header, index == null ? vertices.size() : index.size());
		this.vertices = List.copyOf(vertices);
		this.index = Optional.ofNullable(index).map(List::copyOf);
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
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
			private final int len = vertices.size() * header().layout().size() * Float.BYTES;

			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				final Layout layout = header().layout();
				for(final Vertex v : vertices) {
					layout.buffer(v, buffer);
				}
			}
		};
	}

	@Override
	public Optional<Bufferable> indexBuffer() {
		return index.map(DefaultModel::index);
	}

	/**
	 * Creates the index buffer.
	 */
	private static Bufferable index(List<Integer> index) {
		return new Bufferable() {
			@Override
			public int length() {
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
		return new BufferedModel(header(), count(), vertexBuffer(), indexBuffer().orElse(null));
	}

	/**
	 * Builder for a vertex model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private boolean clockwise;
		private Layout layout = new Layout(Component.POSITION);

		protected final List<Vertex> vertices = new ArrayList<>();
		protected List<Integer> index;

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
		public DefaultModel build() {
			return new DefaultModel(new Header(primitive, layout, clockwise), vertices, index);
		}
	}

	/**
	 * Builder for an indexed model.
	 * TODO - doc, auto
	 */
	public static class IndexedBuilder extends Builder {
		private final Map<Vertex, Integer> map = new HashMap<>();

		public IndexedBuilder() {
			index = new ArrayList<>();
		}

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
	}
}
