package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>default model</i> is comprised of vertices and an optional index buffer.
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
public class DefaultModel extends AbstractModel {
	private final List<Vertex> vertices;
	private final int[] index;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @param index			Index
	 */
	public DefaultModel(Primitive primitive, List<Layout> layout, List<Vertex> vertices, List<Integer> index) {
		super(primitive, layout);
		this.vertices = List.copyOf(vertices);
		this.index = index.stream().mapToInt(Integer::intValue).toArray();
		validate(false);
	}

	@Override
	public boolean isIndexed() {
		return index.length > 0;
	}

	@Override
	public int count() {
		if(isIndexed()) {
			return index.length;
		}
		else {
			return vertices.size();
		}
	}

	@Override
	public Bufferable vertices() {
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
	public Bufferable index() {
		return new Bufferable() {
			private final int len = index.length * Integer.BYTES;

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
					buffer.put(index);
				}
			}
		};
	}
}
