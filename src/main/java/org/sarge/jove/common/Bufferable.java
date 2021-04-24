package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
		// Buffer this object
		final int len = this.length();
		final ByteBuffer bb = VulkanHelper.buffer(len); // TODO - needs to be direct?
		buffer(bb);

		// Convert to array
		if(bb.isDirect()) {
			final byte[] array = new byte[len];
			bb.rewind();
			bb.get(array);
			return array;
		}
		else {
			return bb.array();
		}
	}

	// TODO
	static Bufferable of(Bufferable... objects) {
		return new Bufferable() {
			@Override
			public int length() {
				return Arrays.stream(objects).mapToInt(Bufferable::length).sum();
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Bufferable obj : objects) {
					obj.buffer(buffer);
				}
			}
		};
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
