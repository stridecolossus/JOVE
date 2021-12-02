package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

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
public class BufferWrapper {
	/**
	 * Native byte order for a bufferable object.
	 */
	public static final ByteOrder ORDER = ByteOrder.nativeOrder();

	/**
	 * Allocates a direct byte buffer of the given length with native order.
	 * @param len Buffer length
	 * @return New byte buffer
	 */
	public static ByteBuffer allocate(int len) {
		return ByteBuffer.allocateDirect(len).order(ORDER);
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
	 * Constructor given an array.
	 * @param array Array
	 */
	public static ByteBuffer buffer(byte[] array) {
		final ByteBuffer bb = allocate(array.length);
		write(array, bb);
		return bb;
	}

	private final ByteBuffer bb;

	/**
	 * Constructor.
	 * @param bb Byte buffer
	 */
	public BufferWrapper(ByteBuffer bb) {
		this.bb = notNull(bb);
	}

	/**
	 * @return Underlying byte buffer
	 */
	public ByteBuffer buffer() {
		return bb;
	}

	/**
	 * Converts this buffer to an array.
	 * @return Array
	 */
	public byte[] array() {
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
	 * Rewinds this buffer.
	 */
	public BufferWrapper rewind() {
		bb.rewind();
		return this;
	}

	/**
	 * Adds the given data to this buffer.
	 * @param data Data
	 * @see Bufferable#buffer(ByteBuffer)
	 */
	public BufferWrapper append(Bufferable data) {
		data.buffer(bb);
		return this;
	}

	/**
	 * Inserts a data <i>element</i> into this buffer.
	 * @param index			Element index
	 * @param data			Data
	 */
	public BufferWrapper insert(int index, Bufferable data) {
		final int pos = index * data.length();
		bb.position(pos);
		data.buffer(bb);
		return this;
	}

	@Override
	public String toString() {
		return bb.toString();
	}
}
