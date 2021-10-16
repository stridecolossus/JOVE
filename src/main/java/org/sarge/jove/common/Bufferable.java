package org.sarge.jove.common;

import static org.sarge.jove.common.Bufferable.write;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A <i>bufferable</i> is a data object that can be written to an NIO buffer.
 * @author Sarge
 */
@SuppressWarnings("unused")
public interface Bufferable {
	/**
	 * Native byte order for a bufferable object.
	 */
	ByteOrder ORDER = ByteOrder.nativeOrder();

	/**
	 * @return Length of this object (bytes)
	 */
	int length();

	/**
	 * Writes this object to the given buffer.
	 * @param buffer Buffer
	 */
	void buffer(ByteBuffer buffer);

	/**
	 * Helper - Writes a byte array to the given buffer.
	 * @param bytes		Byte array
	 * @param bb		Buffer
	 */
	static void write(byte[] bytes, ByteBuffer bb) {
		if(bb.isDirect()) {
			for(byte b : bytes) {
				bb.put(b);
			}
		}
		else {
			bb.put(bytes);
		}
	}

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
			public void buffer(ByteBuffer buffer) {
				write(bytes, buffer);
			}
		};
	}

	/**
	 * Converts the given bufferable object to an array of bytes.
	 * @param obj Bufferable object
	 * @return New byte array
	 */
	static byte[] toArray(Bufferable obj) {
		final int len = obj.length();
		final ByteBuffer bb = ByteBuffer.allocate(len).order(ORDER);
		obj.buffer(bb);
		return bb.array();
	}
}
