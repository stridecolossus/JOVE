package org.sarge.jove.io;

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
	 * Converts the given byte buffer to an array.
	 * @param bb Byte buffer
	 * @return Array
	 */
	public static byte[] toArray(ByteBuffer bb) {
		if(bb.isDirect()) {
			bb.rewind();
			final int len = bb.limit();
			final byte[] bytes = new byte[len];
			for(int n = 0; n < len; ++n) {
				bytes[n] = bb.get();
			}
			return bytes;
		}
		else {
			return bb.array();
		}
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
