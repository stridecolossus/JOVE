package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.AbstractModel;

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
	private final Optional<int[]> index;

	/**
	 * Constructor.
	 * @param header			Model header
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	protected DefaultModel(Header header, List<Vertex> vertices, int[] index) {
		super(header);
		this.vertices = notNull(vertices);
		this.index = Optional.ofNullable(index);
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
	}

	@Override
	public Bufferable vertices() {
		return new Bufferable() {
			private final int len = vertices.size() * Layout.stride(header.layout());

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
	// TODO - non-interleaved option? i.e. would have to select each component from each vertex

	@Override
	public Optional<Bufferable> index() {
		return index.map(DefaultModel::index);
	}

	/**
	 * Builds the index buffer.
	 */
	private static Bufferable index(int[] index) {
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
	// TODO - implementations for integer and short?
}
