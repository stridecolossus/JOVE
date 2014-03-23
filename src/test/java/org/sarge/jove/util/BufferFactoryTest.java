package org.sarge.jove.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.Test;
import org.sarge.jove.util.BufferFactory;

public class BufferFactoryTest {
	@Test
	public void createIntegerBuffer() {
		final IntBuffer buffer = BufferFactory.createIntegerBuffer( 3 );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
	}

	@Test
	public void createIntegerBufferArray() {
		final IntBuffer buffer = BufferFactory.createIntegerBuffer( new int[]{ 1, 2, 3 } );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
		for( int n = 1; n <= 3; ++n ) {
			assertEquals( n, buffer.get() );
		}
	}
	
	@Test
	public void createFloatBuffer() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( 3 );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
	}

	@Test
	public void createFloatBufferArray() {
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( new float[]{ 1, 2, 3 } );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
		for( int f = 1; f <= 3; ++f ) {
			assertEquals( f, buffer.get(), 0.0001f );
		}
	}
	
	@Test
	public void createByteBuffer() {
		final ByteBuffer buffer = BufferFactory.createByteBuffer( 3 );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
	}

	@Test
	public void createByteBufferArray() {
		final ByteBuffer buffer = BufferFactory.createByteBuffer( new byte[]{ 1, 2, 3 } );
		assertEquals( ByteOrder.nativeOrder(), buffer.order() );
		check( buffer );
		for( byte b = 1; b <= 3; ++b ) {
			assertEquals( b, buffer.get() );
		}
	}
	
	private void check( Buffer buffer ) {
		assertNotNull( buffer );
		assertEquals( 3, buffer.capacity() );
		assertEquals( 3, buffer.limit() );
		assertEquals( 0, buffer.position() );
		assertEquals( true, buffer.isDirect() );
	}
}
