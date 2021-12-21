package org.sarge.jove.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A <i>buffer wrapper</i> is a convenience wrapper for an NIO byte buffer.
 * <p>
 * This class provides convenience methods to support uniform buffer, push constants, etc.
 * <p>
 * Example:
 * <p>
 * <pre>
 * 	// Create a buffer wrapper
 * 	ByteBuffer bb = ...
 * 	BufferWrapper buffer = new BufferWrapper(bb);
 *
 * 	// Incrementally add data to the buffer
 * 	buffer.rewind();
 * 	buffer.append(data);
 *
 * 	// Insert data as a random access array
 * 	buffer.insert(1, data);
 * </pre>
 * <p>
 * Additionally the following methods can be used to transform to/from byte buffers and primitive arrays:
 * <ul>
 * <li>{@link #array()} converts a buffer to a byte array</li>
 * <li>{@link #allocate(int)} can be used to allocate a <i>direct</i> buffer</li>
 * <li>{@link #write(byte[], ByteBuffer)} copies an array to a buffer</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>Direct byte buffers generally do <b>not</b> support the optional bulk transfer methods, e.g. {@link Buffer#array()}</li>
 * <li>The majority of the underlying {@link Buffer} methods are {@code final} with implications for testing</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public final class BufferHelper {
	/**
	 * Native byte order for a bufferable object.
	 */
	public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

	private BufferHelper() {
	}

	/**
	 * Allocates a <b>direct</b> byte buffer of the given length with {@link #NATIVE_ORDER}.
	 * @param len Buffer length
	 * @return New byte buffer
	 */
	public static ByteBuffer allocate(int len) {
		return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
	}

	/**
	 * Converts a byte buffer to an array.
	 * @param bb Byte buffer
	 * @return Byte array
	 */
	public static byte[] array(ByteBuffer bb) {
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
	 * Writes an array to the given byte buffer.
	 * @param array		Byte array
	 * @param bb		Buffer
	 */
	public static void write(byte[] array, ByteBuffer bb) {
		if(bb.isDirect()) {
			for(byte b : array) {
				bb.put(b);
			}
		}
		else {
			bb.put(array);
		}
	}

	/**
	 * Creates a byte buffer wrapping the given byte array.
	 * @param array Byte array
	 * @return Byte buffer
	 */
	public static ByteBuffer buffer(byte[] array) {
		final ByteBuffer bb = allocate(array.length);
		write(array, bb);
		return bb;
	}
}
