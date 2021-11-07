package org.sarge.jove.common;

import java.nio.ByteBuffer;

import org.sarge.jove.util.BufferHelper;

/**
 * A <i>bufferable</i> is a data object that can be written to an NIO buffer.
 * @author Sarge
 */
public interface Bufferable {
	/**
	 * @return Length of this object (bytes)
	 */
	int length();

	/**
	 * Writes this object to the given buffer.
	 * @param bb Buffer
	 */
	void buffer(ByteBuffer bb);

	/**
	 * Creates a bufferable wrapping the given array.
	 * @param bytes Byte array
	 * @return Bufferable wrapper
	 */
	static Bufferable of(byte[] bytes) {
		return new Bufferable() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				BufferHelper.write(bytes, bb);
			}
		};
	}
}
