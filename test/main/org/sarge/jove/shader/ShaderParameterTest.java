package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class ShaderParameterTest {
	private ShaderParameter param;

	@Before
	public void before() {
		param = new ShaderParameter( "param", ParameterType.FLOAT, 42, 1, 1 );
	}

	@Test
	public void constructor() {
		assertEquals( "param", param.getName() );
		assertEquals( 42, param.getLocation() );
		assertEquals( 1, param.getSize() );
		assertEquals( ParameterType.FLOAT, param.getType() );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void isDirty() {
		// Set value and check dirty
		param.setValue( 1f );
		assertEquals( true, param.isDirty() );

		// Read value and check no longer dirty
		param.getValue();
		assertEquals( false, param.isDirty() );

		// Reset another value and check dirty again
		param.setValue( 2f );
		assertEquals( true, param.isDirty() );
	}

	@Test
	public void setValue() {
		param.setValue( 42f );
		assertEquals( 42f, param.getValue() );
	}

	@Test
	public void setValueArray() {
		param = new ShaderParameter( "param", ParameterType.FLOAT, 42, 1, 3 );
		param.setValue( new float[]{ 1, 2, 3 } );
		assertNotNull( param.getValue() );
	}

	@Test( expected = IllegalArgumentException.class )
	public void setValueWrongClass() {
		param.setValue( new Object() );
	}

	@Test( expected = IllegalArgumentException.class )
	public void setValueNotArray() {
		param.setValue( new float[]{ 1, 2 } );
	}

	@Test( expected = IllegalArgumentException.class )
	public void setValueWrongLength() {
		param = new ShaderParameter( "param", ParameterType.FLOAT, 42, 1, 3 );
		param.setValue( new float[]{ 1, 2 } );
	}

	// TODO - check buffering
}
