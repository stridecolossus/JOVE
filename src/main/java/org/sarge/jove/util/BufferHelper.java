package org.sarge.jove.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.sarge.jove.common.Bufferable;

/**
 * The <i>buffer helper</i> provides utility methods for managing NIO byte buffers.
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

	/**
	 * Maximum length of a {@code short} buffer.
	 */
	private static final long SHORT = MathsUtil.unsignedMaximum(Short.SIZE);

	private BufferHelper() {
	}

	/**
	 * Determines whether the given draw count can be represented by a <i>short</i> index.
	 * @param count Draw count
	 * @return Whether index is short
	 */
	public static boolean isShortIndex(long count) {
		return count < BufferHelper.SHORT;
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

	/**
	 * Inserts a data <i>element</i> into the given buffer.
	 * <p>
	 * This method is intended for populating uniform or push constant buffers that are essentially an array of some object.
	 * <p>
	 * For example a uniform buffer containing the projection and modelview matrices could be populated as follows:
	 * <p>
	 * <pre>
	 * // Create backing buffer
	 * ByteBuffer bb = BufferHelper.allocate(3 * Matrix.IDENTITY.length());
	 *
	 * // Init projection matrix (once)
	 * Matrix projection = ...
	 * BufferHelper.insert(2, projection, bb);
	 *
	 * ...
	 *
	 * // Populate modelview matrix (each frame)
	 * bb.rewind();
	 * view.buffer(bb);
	 * model.buffer(bb);
	 * </pre>
	 * <p>
	 * @param index			Object index
	 * @param data			Bufferable object
	 * @param bb			Byte buffer
	 */
	public static void insert(int index, Bufferable data, ByteBuffer bb) {
		final int pos = index * data.length();
		bb.position(pos);
		data.buffer(bb);
	}
}
