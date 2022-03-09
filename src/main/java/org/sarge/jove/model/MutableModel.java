package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>mutable model</i> is used to construct vertex data and an optional index for a model.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as <b>direct</b> NIO buffers</li>
 * </ul>
 * Usage:
 * <pre>
 * // Init model for coloured lines
 * MutableModel model = new MutableModel()
 *     .primitive(Primitive.LINES)
 *     .layout(Point.LAYOUT)
 *     .layout(Colour.LAYOUT);
 *
 * // Add some vertices
 * Vertex vertex = new Vertex()
 *     .position(new Point(...))
 *     .colour(new Colour(...));
 * model.add(vertex);
 * ...
 *
 * // Build index
 * model.add(0).add(1);
 * ...
 *
 * // Generate buffers
 * Bufferable vbo = model.vertexBuffer();
 * Bufferable index = model.indexbuffer().get();
 * </pre>
 * @see Vertex
 * @author Sarge
 */
public class MutableModel implements Model {
	private Primitive primitive = Primitive.TRIANGLE_STRIP;
	private final List<Layout> layout = new ArrayList<>();
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Integer> index = new ArrayList<>();

	@Override
	public Primitive primitive() {
		return primitive;
	}

	/**
	 * Sets the drawing primitive for this model (default is {@link Primitive#TRIANGLE_STRIP}).
	 * @param primitive Drawing primitive
	 */
	public MutableModel primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	@Override
	public List<Layout> layout() {
		return List.copyOf(layout);
	}

	/**
	 * Adds a vertex layout to this model.
	 * @param layout Vertex layout
	 */
	public MutableModel layout(Layout layout) {
		this.layout.add(notNull(layout));
		return this;
	}

	/**
	 * Helper - Adds multiple vertex layouts to this model.
	 * @param layouts Vertex layouts
	 */
	public MutableModel layout(List<Layout> layouts) {
		this.layout.addAll(layouts);
		return this;
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
	 * @return Vertex data
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex.
	 * <p>
	 * Note that this method does not validate or make any assumptions regarding the components of the vertex.
	 * i.e. it is the responsibility of the user to ensure that vertices match the layout of this model.
	 * <p>
	 * @param v Vertex
	 */
	public MutableModel add(Vertex v) {
		vertices.add(notNull(v));
		return this;
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream().mapToInt(Integer::intValue);
	}

	/**
	 * Adds an index.
	 * @param n Index
	 * @throws IllegalArgumentException if the index is invalid for this model
	 */
	public MutableModel add(int n) {
		if((n < 0) ||(n >= vertices.size())) throw new IllegalArgumentException(String.format("Invalid index: index=%d vertices=%d", n, vertices.size()));
		index.add(n);
		return this;
	}

	/**
	 * Adds a primitive restart index.
	 */
	public MutableModel restart() {
		index.add(-1);
		return this;
	}

	@Override
	public Bufferable vertexBuffer() {
		return new Bufferable() {
			@Override
			public int length() {
				return vertices.size() * Layout.stride(layout);
			}

			@Override
			public void buffer(ByteBuffer bb) {
				for(final Vertex v : vertices) {
					v.buffer(bb);
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
						for(final int n : index) {
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
					for(final int n : index) {
						bb.putShort((short) n);
					}
				}
			}
		};
		return Optional.of(buffer);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append(layout)
				.append("indexed", !index.isEmpty())
				.append("count", count())
				.build();
	}
}
