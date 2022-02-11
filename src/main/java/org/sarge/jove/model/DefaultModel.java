package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>default model</i> is a standard implementation that also provides accessors for the model vertices and index.
 * <p>
 * Note that the {@link #vertexBuffer()} and {@link #indexBuffer()} are constructed on demand.
 * <p>
 * @author Sarge
 */
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices;
	private final List<Integer> index;

	/**
	 * Constructor.
	 * @param primitive			Drawing primitive
	 * @param layout			Vertex layout
	 * @param vertices			Vertices
	 * @param index				Index
	 */
	DefaultModel(Primitive primitive, List<Layout> layout, List<Vertex> vertices, List<Integer> index) {
		super(primitive, layout);
		this.vertices = vertices;
		this.index = index;
	}

	@Override
	public int count() {
		if(index.isEmpty()) {
			return vertices.size();
		}
		else {
			return index.size();
		}
	}

	/**
	 * @return Vertices
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream().mapToInt(Integer::intValue);
	}

	@Override
	public Bufferable vertexBuffer() {
		return new Bufferable() {
			@Override
			public int length() {
				return vertices.size() * Layout.stride(layout());
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Vertex v : vertices) {
					v.buffer(buffer);
				}
			}
		};
	}

	@Override
	public Optional<Bufferable> indexBuffer() {
		// Check whether indexed
		if(index.isEmpty()) {
			return Optional.empty();
		}

		// Determine whether the index can be represented by short integers
		final boolean integral = Model.isIntegerIndex(vertices.size());

		// Build index buffer
		final Bufferable buffer = new Bufferable() {
			@Override
			public int length() {
				return index.size() * (integral ? Integer.BYTES : Short.BYTES);
			}

			@Override
			public void buffer(ByteBuffer bb) {
				if(integral) {
					if(bb.isDirect()) {
						// Write index to direct integer buffer
						for(int n : index) {
							bb.putInt(n);
						}
					}
					else {
						// Write index to non-direct integer buffer
						final int[] array = index().toArray();
						bb.asIntBuffer().put(array);
					}
				}
				else {
					// Otherwise write index as a short buffer
					for(int n : index) {
						bb.putShort((short) n);
					}
				}
			}
		};
		return Optional.of(buffer);
	}
}
