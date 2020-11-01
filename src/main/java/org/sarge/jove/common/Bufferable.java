package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A <i>bufferable</i> object can be written to an NIO buffer.
 * @author Sarge
 */
public interface Bufferable {
	/**
	 * Writes this object to the given buffer.
	 * @param buffer Buffer
	 */
	void buffer(ByteBuffer buffer);

	/**
	 * @return Length of this object (bytes)
	 */
	long length();

	/**
	 * Native byte order for NIO buffers.
	 */
	ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

	/**
	 * Wraps the given byte-buffer as a bufferable.
	 * @param bb Byte-buffer
	 * @return Wrapped bufferable
	 */
	static Bufferable of(ByteBuffer bb) {
		return new Bufferable() {
			@Override
			public long length() {
				return bb.remaining();
			}

			@Override
			public void buffer(ByteBuffer dest) {
				dest.put(bb);
			}
		};
	}

	/**
	 * Allocates a direct NIO buffer of the given length.
	 * @param len Length
	 * @return New direct buffer
	 */
	static ByteBuffer allocate(int len) {
		return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
	}

	/**
	 * Allocates a direct NIO buffer that wraps the given array.
	 * @param bytes Array
	 * @return New NIO buffer
	 */
	static ByteBuffer allocate(byte[] bytes) {
		final ByteBuffer bb = allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		return bb;
	}
}
