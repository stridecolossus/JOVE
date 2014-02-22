package org.sarge.jove.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Utility methods for allocating and managing NIO buffers.
 * @author Sarge
 */
public class BufferUtils {
	private static final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
	private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;
	private static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

	private BufferUtils() {
		// Utility class
	}

	/**
	 * Creates an integer buffer of the given length.
	 * @param len Length
	 * @return Integer buffer
	 */
	public static IntBuffer createIntegerBuffer( int len ) {
		return createByteBuffer( len * INTEGER_SIZE ).asIntBuffer();
	}

	/**
	 * Creates an integer buffer from the given array.
	 * @param data Integer data
	 * @return Integer buffer
	 */
	public static IntBuffer createIntegerBuffer( int[] data ) {
		final IntBuffer buffer = createIntegerBuffer( data.length );
		for( int n : data ) buffer.put( n );
		buffer.rewind();
		return buffer;
	}

	/**
	 * Creates a floating-point buffer of the given length.
	 * @param len Length
	 * @return Float buffer
	 */
	public static FloatBuffer createFloatBuffer( int len ) {
		return createByteBuffer( len * FLOAT_SIZE ).asFloatBuffer();
	}

	/**
	 * Creates a floating-point buffer from the given array.
	 * @param data Floating-point data
	 * @return Float buffer
	 */
	public static FloatBuffer createFloatBuffer( float[] data ) {
		final FloatBuffer buffer = createFloatBuffer( data.length );
		for( float f : data ) buffer.put( f );
		buffer.rewind();
		return buffer;
	}

	/**
	 * Creates a direct byte buffer of the given length.
	 * @param len Length
	 * @return Direct byte buffer
	 */
	public static ByteBuffer createByteBuffer( int len ) {
		return ByteBuffer.allocateDirect( len ).order( NATIVE_ORDER );
	}

	/**
	 * Creates a byte buffer from the given array.
	 * @param data Bytes
	 * @return Byte buffer
	 */
	public static ByteBuffer createByteBuffer( byte[] data ) {
		final ByteBuffer buffer = createByteBuffer( data.length );
		for( byte b : data ) buffer.put( b );
		buffer.flip();
		return buffer;
	}
}
