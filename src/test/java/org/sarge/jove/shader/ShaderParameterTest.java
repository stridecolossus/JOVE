package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.TextureCoord;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.BufferFactory;

@SuppressWarnings("unused")
public class ShaderParameterTest {
	private ShaderProgram shader;

	@Before
	public void before() {
		shader = mock( ShaderProgram.class );
	}

	private void checkBuffer( int... expected ) {
		// Check data buffered and passed to shader
		final ArgumentCaptor<IntBuffer> captor = ArgumentCaptor.forClass( IntBuffer.class );
		verify( shader ).setInteger( eq( 123 ), captor.capture() );

		// Verify buffer
		final IntBuffer buffer = captor.getValue();
		assertNotNull( buffer );
		assertEquals( expected.length, buffer.capacity() );
		assertEquals( expected.length, buffer.limit() );

		// Verify data
		buffer.rewind();
		for( int n = 0; n < expected.length; ++n ) {
			assertEquals( expected[ n ], buffer.get() );
		}
	}

	private void checkBuffer( int size, float... expected ) {
		// Check data buffered and passed to shader
		final ArgumentCaptor<FloatBuffer> captor = ArgumentCaptor.forClass( FloatBuffer.class );
		verify( shader ).setFloat( eq( 123 ), eq( size ), captor.capture() );

		// Verify buffer
		final FloatBuffer buffer = captor.getValue();
		assertNotNull( buffer );
		assertEquals( expected.length, buffer.capacity() );
		assertEquals( expected.length, buffer.limit() );

		// Verify data
		buffer.rewind();
		for( int n = 0; n < size; ++n ) {
			assertFloatEquals( expected[ n ], buffer.get() );
		}
	}

	@Test
	public void constructor() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.MATRIX, 4, 2, 123 );
		assertEquals( "name", param.getName() );
		assertEquals( ParameterType.MATRIX, param.getType() );
		assertEquals( false, param.isDirty() );
	}

	@Test
	public void setInteger() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.INTEGER, 1, 1, 123 );
		param.set( 42, shader );
		checkBuffer( 42 );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setBoolean() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.BOOLEAN, 1, 1, 123 );
		param.set( true, shader );
		checkBuffer( 1 );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setIntegerArray() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.INTEGER, 1, 3, 123 );
		final int[] array = new int[]{ 1, 2, 3 };
		param.set( array, shader );
		checkBuffer( array );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setFloat() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 1, 123 );
		param.set( 42f, shader );
		checkBuffer( 1, 42f );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setFloatArray() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 3, 123 );
		final float[] array = new float[]{ 1, 2, 3 };
		param.set( array, shader );
		checkBuffer( 1, array );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setBufferable() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 3, 1, 123 );
		final Vector vec = new Vector( 1, 2, 3 );
		param.set( vec, shader );
		checkBuffer( 3, new float[]{ 1, 2, 3 } );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setBufferableArray() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 3, 2, 123 );
		final Vector vec = new Vector( 1, 2, 3 );
		param.set( new Bufferable[]{ vec, vec }, shader );
		checkBuffer( 3, new float[]{ 1, 2, 3, 1, 2, 3 } );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setMatrix() {
		// Create a matrix parameter and populate
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.MATRIX, 4, 1, 123 );
		param.set( Matrix.IDENTITY, shader );
		verify( shader ).setMatrix( 123, 4, param.getTransformer().getFloatBuffer() );

		// Build expected buffer
		final float[] expected = new float[ 4 * 4 ];
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( expected.length );
		Matrix.IDENTITY.append( buffer );
		buffer.flip();
		buffer.get( expected );

		// TODO - modify this test to use getters rather than captors
		FloatBuffer buff = param.getTransformer().getFloatBuffer();
		buff.rewind();
		for(int n=0;n<16;++n){
			assertFloatEquals(expected[n],buff.get());
		}

		//checkBuffer( 4, expected );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setMatrixArray() {
		// Create a 2-size 4x4 matrix parameter and populate
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.MATRIX, 4, 2, 123 );
		param.set( new Matrix[]{ Matrix.IDENTITY, Matrix.IDENTITY }, shader );
		verify( shader ).setMatrix( 123, 4, param.getTransformer().getFloatBuffer() );

		// Build expected buffer
		final float[] expected = new float[ 2 * 4 * 4 ];
		final FloatBuffer buffer = BufferFactory.createFloatBuffer( expected.length );
		Matrix.IDENTITY.append( buffer );
		Matrix.IDENTITY.append( buffer );
		buffer.flip();
		buffer.get( expected );

		// TODO - modify this test to use getters rather than captors
		FloatBuffer buff = param.getTransformer().getFloatBuffer();
		buff.rewind();
		for(int n=0;n<32;++n){
			assertFloatEquals(expected[n],buff.get());
		}

		//checkBuffer( 4, expected );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void dispose() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 1, 123 );
		param.dispose();
		param.set( 42f, shader );
		checkBuffer( 1, 42f );
	}

	@Test(expected=IllegalArgumentException.class)
	public void setInvalidDataType() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 1, 123 );
		param.set( true, shader );
	}

	@Test(expected=IllegalArgumentException.class)
	public void setInvalidArrayLength() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 2, 123 );
		param.set( new float[]{ 1, 2, 3 }, shader );
	}

	@Test(expected=IllegalArgumentException.class)
	public void setInvalidComponentSize() {
		final ShaderParameter param = new ShaderParameter( "name", ParameterType.FLOAT, 1, 3, 123 );
		param.set( new TextureCoord(), shader );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void invalidIntegerSize() {
		new ShaderParameter( "name", ParameterType.INTEGER, 2, 1, 123 );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void invalidBooleanSize() {
		new ShaderParameter( "name", ParameterType.BOOLEAN, 2, 1, 123 );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void invalidBooleanLength() {
		new ShaderParameter( "name", ParameterType.BOOLEAN, 1, 2, 123 );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void invalidTextureSize() {
		new ShaderParameter( "name", ParameterType.TEXTURE, 2, 1, 123 );
	}

	@Test(expected=IllegalArgumentException.class)
	public void invalidMatrixSize() {
		new ShaderParameter( "name", ParameterType.MATRIX, 1, 1, 123 );
	}
}
