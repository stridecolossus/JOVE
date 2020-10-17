package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.DefaultModel;
import org.sarge.jove.util.Check;

/**
 * An <i>indexed model</i> also contains an index buffer.
 * @author Sarge
 */
public class IndexedModel extends DefaultModel {
	private final ByteBuffer index;

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @param index			Index buffer
	 * @throws IllegalArgumentException if the number of vertices does not match the drawing primitive
	 * @throws IllegalArgumentException if the model contains normals but the primitive does not (see {@link Primitive#hasNormals()})
	 * @see Primitive#isValidVertexCount(int)
	 */
	protected IndexedModel(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices, ByteBuffer index, int count) {
		super(primitive, layout, vertices, count);
		this.index = index.asReadOnlyBuffer();
	}

	@Override
	public Optional<ByteBuffer> index() {
		return Optional.of(index);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("index", index.limit())
				.build();
	}

	/**
	 * Builder for an indexed model.
	 */
	public static class IndexedBuilder extends Model.Builder<IndexedBuilder> {
		private final List<Integer> index = new ArrayList<>();

		/**
		 * Adds an index.
		 * @param index Index
		 * @throws IndexOutOfBoundsException if the index is out-of-bounds for the vertices of this model
		 */
		public IndexedBuilder add(int index) {
			Check.zeroOrMore(index);
			if(index >= vertices.size()) throw new IndexOutOfBoundsException(String.format("Invalid vertex index: index=%d vertices=%s", index, vertices.size()));
			this.index.add(index);
			return this;
		}

		/**
		 * Looks up the index of the given vertex.
		 * @param vertex Vertex
		 * @return Vertex index
		 */
		public int indexOf(Vertex vertex) {
			return vertices.indexOf(vertex);
		}

		@Override
		public Model build() {
			final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
			final ByteBuffer buffer = Bufferable.allocate(array.length * Integer.BYTES);
			buffer.asIntBuffer().put(array);
			return new IndexedModel(primitive, layout, vertices, buffer, array.length);
		}
	}

	/**
	 * Creates an indexed builder that performs de-duplication of vertices.
	 * TODO
	 * @return New de-duplication indexed builder
	 */
	public static IndexedBuilder duplicateIndexedBuilder() {
		return new IndexedBuilder() {
			private final Map<Vertex, Integer> map = new HashMap<>();

			@Override
			public int indexOf(Vertex vertex) {
				final Integer index = map.get(vertex);
				if(index == null) throw new IllegalArgumentException("Vertex not present: " + vertex);
				return index;
			}

			@Override
			public IndexedBuilder add(Vertex vertex) {
				final Integer prev = map.get(vertex);
				if(prev == null) {
					// Add index for new vertex
					final int idx = vertices.size();
					super.add(vertex);
					add(idx);
					map.put(vertex, idx);
				}
				else {
					// Otherwise use existing index
					add(prev);
				}
				return this;
			}
		};
	}
}
