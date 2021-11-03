package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Vertex;
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
	private final int[] index;

	/**
	 * Constructor.
	 * @param header			Model header
	 * @param vertices			Vertices
	 * @param index				Optional index
	 */
	public DefaultModel(Header header, List<Vertex> vertices, int[] index) {
		super(header);
		this.vertices = List.copyOf(vertices);
		this.index = index == null ? null : Arrays.copyOf(index, index.length); // TODO - ugly
	}

	@Override
	public boolean isIndexed() {
		return index != null;
	}

	@Override
	public Bufferable vertices() {
		return new Bufferable() {
			private final int len = vertices.size() * header.layout().stride();

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
	public Optional<Bufferable> index() {
		if(index == null) {
			return Optional.empty();
		}

		final Bufferable buffer = new Bufferable() {
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
		return Optional.of(buffer);
	}
}
