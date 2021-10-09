package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.sarge.jove.model.Model.AbstractModel;

/**
 * A <i>buffered model</i> is an implementation with the vertices and index stored as a {@link ByteSource}.
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
	private final ByteBuffer vertices;
	private final Optional<ByteBuffer> index;

	/**
	 * Constructor.
	 * @param header		Model header
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedModel(Header header, ByteBuffer vertices, Optional<ByteBuffer> index) {
		super(header);
		this.vertices = notNull(vertices);
		this.index = notNull(index);
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
	}

	@Override
	public ByteBuffer vertexBuffer() {
		return vertices;
	}

	@Override
	public Optional<ByteBuffer> indexBuffer() {
		return index;
	}
}
