package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.IntegerList;

/**
 * TODO
 * A <i>mutable model</i> is comprised of vertices and an optional index buffer.
 * <p>
 * Notes:
 * <ul>
 * <li>Buffers are generated on-demand</li>
 * <li>The vertex buffer is interleaved</li>
 * <li>Generated buffers are implemented as <b>direct</b> NIO buffers</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class MutableModel extends AbstractModel {
	protected final List<Vertex> vertices = new ArrayList<>();
	protected final IntegerList index = new IntegerList();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 */
	public MutableModel(Primitive primitive, List<Layout> layout) {
		super(primitive, layout);
	}

	@Override
	public boolean isIndexed() {
		return index.size() > 0;
	}

	@Override
	public int count() {
		if(isIndexed()) {
			return index.size();
		}
		else {
			return vertices.size();
		}
	}

	/**
	 * @return Whether this model is empty
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	/**
	 * @return Vertices
	 */
	public Stream<Vertex> vertices() {
		return vertices.stream();
	}

	/**
	 * Adds a vertex.
	 * @param v Vertex
	 */
	public MutableModel add(Vertex v) {
		// TODO - validate
		vertices.add(notNull(v));
		return this;
	}

	/**
	 * @return Index
	 */
	public IntStream index() {
		return index.stream();
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

	@Override
	public Bufferable vertexBuffer() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(layout());

			@Override
			public int length() {
				return len;
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
	public Bufferable indexBuffer() {
		return index.bufferable();
	}
}
