package org.sarge.jove.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Factory methods for NIO buffers.
 * @author Sarge
 */
public final class BufferFactory {
	private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();
	private static final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
	private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

	private BufferFactory() {
	}

	/**
	 * Creates a byte buffer.
	 * @param size Buffer size
	 * @return Byte buffer
	 */
	public static ByteBuffer byteBuffer(int size) {
		return ByteBuffer.allocateDirect(size).order(NATIVE_ORDER);
	}

	/**
	 * Creates a floating-point buffer.
	 * @param size Buffer size
	 * @return Float buffer
	 */
	public static FloatBuffer floatBuffer(int size) {
		return byteBuffer(size * FLOAT_SIZE).asFloatBuffer();
	}

	/**
	 * Creates an integer buffer.
	 * @param size Buffer size
	 * @return Integer buffer
	 */
	public static IntBuffer intBuffer(int size) {
		return byteBuffer(size * INTEGER_SIZE).asIntBuffer();
	}
}
