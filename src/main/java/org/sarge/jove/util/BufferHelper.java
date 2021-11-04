package org.sarge.jove.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Buffer utilities.
 * @author Sarge
 */
public final class BufferHelper {
	/**
	 * Native byte order for a bufferable object.
	 */
	public static final ByteOrder ORDER = ByteOrder.nativeOrder();

	private BufferHelper() {
	}

	/**
	 * Allocate a direct byte buffer of the given length.
	 * @param len Length
	 * @return New direct byte-buffer
	 */
	public static ByteBuffer allocate(int len) {
		return ByteBuffer.allocateDirect(len).order(ORDER);
	}

	/**
	 * Creates a direct byte buffer wrapping the given array.
	 * @param bytes Byte array
	 * @return New direct byte buffer
	 */
	public static ByteBuffer buffer(byte[] bytes) {
		final ByteBuffer bb = allocate(bytes.length);
		write(bytes, bb);
		return bb;
	}

	/**
	 * Writes a byte array to the given buffer.
	 * @param bytes		Byte array
	 * @param bb		Buffer
	 */
	public static void write(byte[] bytes, ByteBuffer bb) {
		if(bb.isDirect()) {
			for(byte b : bytes) {
				bb.put(b);
			}
		}
		else {
			bb.put(bytes);
		}
	}
}
