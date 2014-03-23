package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class ParameterTransformerTest {
	private ParameterTransformer trans;

	@Test
	public void transformInteger() {
		// Create integer transformer and populate
		trans = new ParameterTransformer( ParameterType.BufferType.INTEGER_BUFFER, 3 );
		trans.transform( 1, 2, 3 );

		// Verify allocated buffer
		final IntBuffer buffer = trans.getIntegerBuffer();
		assertNotNull( buffer );
		assertEquals( 3, buffer.capacity() );
		assertEquals( 3, buffer.limit() );
		assertEquals( 0, buffer.position() );

		// Verify data
		for( int n = 1; n <= 3; ++n ) {
			assertEquals( n, buffer.get() );
		}
	}

	@Test
	public void transformFloat() {
		// Create float transformer and populate
		trans = new ParameterTransformer( ParameterType.BufferType.FLOAT_BUFFER, 3 );
		trans.transform( 1f, 2f, 3f );

		// Verify allocated buffer
		final FloatBuffer buffer = trans.getFloatBuffer();
		assertNotNull( buffer );
		assertEquals( 3, buffer.capacity() );
		assertEquals( 3, buffer.limit() );
		assertEquals( 0, buffer.position() );

		// Verify data
		for( int n = 1; n <= 3; ++n ) {
			assertFloatEquals( n, buffer.get() );
		}
	}

	@Test
	public void transformAppendable() {
		// Create integer transformer and populate
		trans = new ParameterTransformer( ParameterType.BufferType.FLOAT_BUFFER, 6 );
		trans.transform( new Vector( 1, 2, 3 ), new Point( 4, 5, 6 ) );

		// Verify allocated buffer
		final FloatBuffer buffer = trans.getFloatBuffer();
		assertNotNull( buffer );
		assertEquals( 6, buffer.capacity() );
		assertEquals( 6, buffer.limit() );
		assertEquals( 0, buffer.position() );

		// Verify data
		for( int n = 1; n <= 6; ++n ) {
			assertFloatEquals( n, buffer.get() );
		}
	}

	@Test(expected=ClassCastException.class)
	public void getInvalidBuffer() {
		trans = new ParameterTransformer( ParameterType.BufferType.FLOAT_BUFFER, 1 );
		trans.getIntegerBuffer();
	}
}
