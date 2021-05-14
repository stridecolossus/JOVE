package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sarge.jove.common.ByteSource;
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;

/**
 * A <i>default model</i> is comprised of vertices and an optional index buffer.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as direct NIO buffers</li>
 * </ul>
 * <p>
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
		super(header);
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
	public ByteSource vertexBuffer() {
		// Allocate buffer
		final int len = vertices.size() * header().length();
		final ByteBuffer buffer = VulkanHelper.buffer(len);

		// Buffer vertices
		for(Vertex v : vertices) {
			v.buffer(buffer);
		}

		// Create wrapper
		return ByteSource.of(buffer);
	}

	@Override
	public Optional<ByteSource> indexBuffer() {
		return index.map(DefaultModel::index);
	}

	/**
	 * Creates the index buffer.
	 */
	private static ByteSource index(List<Integer> index) {
		// Allocate index buffer
		final int len = index.size() * Integer.BYTES;
		final ByteBuffer bb = VulkanHelper.buffer(len);

		// Buffer index
		// TODO - convert to array first? or change arg to array?
		final IntBuffer buffer = bb.asIntBuffer();
		for(int n : index) {
			buffer.put(n);
		}

		// Create wrapper
		return ByteSource.of(bb);
	}

	public BufferedModel buffer() {
		return new BufferedModel(header, vertexBuffer(), indexBuffer());
	}

	/**
	 * Builder for a model.
	 */
	public static class Builder {
		private Primitive primitive = Primitive.TRIANGLE_STRIP;
		private boolean clockwise;

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
		 * Adds a vertex.
		 * @param v Vertex
		 * @throws IllegalArgumentException if the vertex does not match the configured layout
		 */
		public Builder add(Vertex v) {
			// TODO - check matching vertex
			vertices.add(v);
			return this;
		}

		// TODO
		// compute normals
		// - walk index -> faces (triangles) ~ primitive
		// - accumulate normal @ each vertex of each face (cross product)
		// - normalise all
		// - invalid if no normals
		// implies:
		// - operations on model?
		// - face iterator?
		// - normal accessor and accumulator/mutator

		/**
		 * Constructs this model.
		 * @return New model
		 */
		public DefaultModel build() {
			return build(null, vertices.size());
		}

		/**
		 * Constructs this model.
		 * @param index		Index or {@code null} if not indexed
		 * @param count		Number of vertices
		 * @return New model
		 */
		protected final DefaultModel build(List<Integer> index, int count) {
			final List<Layout> layout = vertices.isEmpty() ? List.of() : vertices.get(0).layout();
			return new DefaultModel(new Header(layout, primitive, count, clockwise), vertices, index);
		}
	}

	/**
	 * Builder for an indexed model.
	 * TODO - doc, auto
	 * TODO - could be a plug in strategy for add() method? i.e. base builder has index but unused unless this plug-in?
	 */
	public static class IndexedBuilder extends Builder {
		private final Map<Vertex, Integer> map = new HashMap<>();
		private final List<Integer> index = new ArrayList<>();

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
		public DefaultModel build() {
			return build(index, index.size());
		}
	}
}
