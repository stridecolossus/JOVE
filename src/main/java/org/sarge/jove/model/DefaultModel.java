package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.AbstractModel;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.lib.util.Check;

/**
 *
 * TODO
 *
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
	private final List<Layout> layout = new ArrayList<>();
	protected final List<Vertex> vertices = new ArrayList<>();
	protected final List<Integer> index = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive Drawing primitive
	 */
	public DefaultModel(Primitive primitive) {
		super(primitive);
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

	@Override
	public List<Layout> layout() {
		return layout;
	}

	/**
	 * Adds a vertex layout to this model.
	 * @param layout Vertex layout
	 */
	public void layout(Layout layout) {
		this.layout.add(notNull(layout));
	}

	/**
	 * Helper - Adds layouts for the given vertex components.
	 * @param components Vertex components
	 */
	public void layout(List<Component> components) {
		components
				.stream()
				.map(Component::layout)
				.forEach(this::layout);
	}

	@Override
	public Bufferable vertices() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(layout);

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

	/**
	 * Adds a vertex to this model.
	 * @param vertex Vertex
	 */
	public void add(Vertex vertex) {
		vertices.add(notNull(vertex));
	}

	@Override
	public boolean isIndexed() {
		return !index.isEmpty();
	}

	@Override
	public Bufferable index() {
		return new Bufferable() {
			private final int len = index.size() * Integer.BYTES;

			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				final IntBuffer buffer = bb.asIntBuffer();
				if(buffer.isDirect()) {
					for(int n : index) {
						buffer.put(n);
					}
				}
				else {
					final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
					buffer.put(array);
				}
			}
		};
	}

	/**
	 * Adds an index to this model.
	 * @param n Index
	 * @throws IllegalArgumentException if the index is larger than the number of vertices
	 */
	public void add(int n) {
		Check.zeroOrMore(n);
		if(n >= vertices.size()) throw new IllegalArgumentException(String.format("Invalid index: index=%d vertices=%d", n, vertices.size()));
		index.add(n);
	}
}
