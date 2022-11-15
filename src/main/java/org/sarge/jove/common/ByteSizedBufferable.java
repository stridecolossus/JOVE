package org.sarge.jove.common;

import java.nio.ByteBuffer;

import org.sarge.jove.io.BufferHelper;

/**
 * A <i>byte sized bufferable</i> is a bufferable object with a specified length.
 * @author Sarge
 */
public interface ByteSizedBufferable extends Bufferable {
	/**
	 * @return Length of this bufferable (bytes)
	 */
	int length();

	/**
	 * Creates a bufferable wrapping the given array.
	 * @param bytes Byte array
	 * @return Bufferable array
	 */
	static ByteSizedBufferable of(byte[] bytes) {
		return new ByteSizedBufferable() {
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
