package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import org.sarge.jove.util.BufferFactory;

/**
 * An <i>index buffer object</i> (IBO) stores vertex indices.
 * @author Sarge
 */
public class IndexBufferObject extends BufferObject {
	/**
	 * Creates an index buffer from the given indices.
	 * @param mode			Update mode
	 * @param int			Length
	 * @param indices		Indices
	 * @return Index buffer
	 */
	public static IndexBufferObject of(Mode mode, int len, IntStream indices) {
		final IntBuffer buffer = BufferFactory.intBuffer(len);
		final IndexBufferObject index = new IndexBufferObject(mode, buffer);
		index.buffer(indices);
		return index;
	}

	private final IntBuffer buffer;

	// TODO - size of elements, e.g. could be bytes for small model

	/**
	 * Constructor.
	 * @param mode			Update mode
	 * @param buffer		Index buffer
	 */
	public IndexBufferObject(Mode mode, IntBuffer buffer) {
		super(mode);
		this.buffer = notNull(buffer);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int length() {
		return buffer.capacity();
	}

	/**
	 * Updates this index buffer.
	 * @param indices New indices
	 * @throws IllegalArgumentException if this buffer is not mutable
	 */
	public void update(IntStream indices) {
		checkMutable();
		buffer(indices);
	}

	/**
	 * Updates this buffer.
	 * @param indices Indices
	 */
	private void buffer(IntStream indices) {
		assert buffer.position() == 0;
		indices.forEach(buffer::put);
		buffer.flip();
	}

	@Override
	public void push() {
		// TODO
	}
}
