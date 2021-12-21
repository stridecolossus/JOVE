package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>buffer wrapper</i> is a convenience wrapper for an NIO byte buffer to support uniform buffers, push constants, etc.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create a buffer wrapper
 * ByteBuffer bb = ...
 * BufferWrapper buffer = new BufferWrapper(bb);
 *
 * // Incrementally add data to the buffer
 * buffer.rewind();
 * buffer.append(data);
 *
 * // Insert data as a random access array
 * buffer.insert(1, data);
 * </pre>
 * <p>
 * @author Sarge
 */
public class BufferWrapper {
	private final ByteBuffer bb;

	/**
	 * Constructor.
	 * @param bb Byte buffer
	 */
	public BufferWrapper(ByteBuffer bb) {
		this.bb = notNull(bb);
	}

	/**
	 * @return Underlying byte buffer
	 */
	public ByteBuffer buffer() {
		return bb;
	}

	/**
	 * Rewinds this buffer.
	 */
	public BufferWrapper rewind() {
		bb.rewind();
		return this;
	}

	/**
	 * Adds the given data to this buffer.
	 * @param data Data
	 * @see Bufferable#buffer(ByteBuffer)
	 */
	public BufferWrapper append(Bufferable data) {
		data.buffer(bb);
		return this;
	}

	/**
	 * Inserts a data <i>element</i> into this buffer.
	 * @param index			Element index
	 * @param data			Data
	 */
	public BufferWrapper insert(int index, Bufferable data) {
		final int pos = index * data.length();
		bb.position(pos);
		data.buffer(bb);
		return this;
	}

	@Override
	public String toString() {
		return bb.toString();
	}
}
