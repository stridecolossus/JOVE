package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParameterTypeTest {
	@Test
	public void map() {
		assertEquals( ParameterType.FLOAT, ParameterType.map( "FLOAT" ) );
		assertEquals( ParameterType.FLOAT, ParameterType.map( "FLOAT_VEC2" ) );
		assertEquals( ParameterType.FLOAT, ParameterType.map( "FLOAT_VEC3" ) );
		assertEquals( ParameterType.FLOAT, ParameterType.map( "FLOAT_VEC4" ) );
		assertEquals( ParameterType.INTEGER, ParameterType.map( "INT" ) );
		assertEquals( ParameterType.BOOLEAN, ParameterType.map( "BOOL" ) );
		assertEquals( ParameterType.MATRIX, ParameterType.map( "FLOAT_MAT2" ) );
		assertEquals( ParameterType.MATRIX, ParameterType.map( "FLOAT_MAT3" ) );
		assertEquals( ParameterType.MATRIX, ParameterType.map( "FLOAT_MAT4" ) );
		assertEquals( ParameterType.TEXTURE, ParameterType.map( "SAMPLER_1D" ) );
		assertEquals( ParameterType.TEXTURE, ParameterType.map( "SAMPLER_2D" ) );
		assertEquals( ParameterType.TEXTURE, ParameterType.map( "SAMPLER_CUBE" ) );
		assertEquals( null, ParameterType.map( "COBBLERS" ) );
	}

	@Test
	public void getBufferType() {
		assertEquals( ParameterType.BufferType.FLOAT_BUFFER, ParameterType.MATRIX.getBufferType() );
		assertEquals( ParameterType.BufferType.FLOAT_BUFFER, ParameterType.FLOAT.getBufferType() );
		assertEquals( ParameterType.BufferType.INTEGER_BUFFER, ParameterType.INTEGER.getBufferType() );
		assertEquals( ParameterType.BufferType.INTEGER_BUFFER, ParameterType.BOOLEAN.getBufferType() );
		assertEquals( ParameterType.BufferType.INTEGER_BUFFER, ParameterType.TEXTURE.getBufferType() );
	}
}
