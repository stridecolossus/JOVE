package org.sarge.jove.common;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.util.VulkanHelper;

/**
 * A <i>bufferable</i> is a data object that can be written to an NIO buffer.
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
	int length();

	/**
	 * Helper - Converts this bufferable object to a byte array.
	 * @return Byte array
	 */
	default byte[] toByteArray() {
		final ByteBuffer bb = VulkanHelper.buffer(length());
		buffer(bb);
		return bb.array();
	}

	/**
	 * Wraps the given byte array as a bufferable.
	 * @param bytes Byte array
	 * @return Wrapped bufferable
	 */
	static Bufferable of(byte[] bytes) {
		return new Bufferable() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				buffer.put(bytes);
			}

			@Override
			public byte[] toByteArray() {
				return bytes;
			}
		};
	}
}
