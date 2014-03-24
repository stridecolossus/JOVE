package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.junit.Before;
import org.junit.Test;

public class AbstractShaderProgramTest {
	/**
	 * Mock implementation.
	 */
	class MockShaderProgram extends AbstractShaderProgram {
		boolean bound;
		boolean attached;
		boolean linked;
		boolean updated;
		boolean deleted;

		protected MockShaderProgram( Shader[] shaders ) {
			super( shaders );
			assertEquals( 1, shaders.length );
			assertEquals( shader, shaders[ 0 ] );
		}

		@Override
		protected int allocate() {
			return 123;
		}

		@Override
		protected void attach( int id, int shaderID ) {
			assertEquals( 123, id );
			assertEquals( 42, shaderID );
			assertEquals( false, attached );
			attached = true;
		}

		@Override
		protected void link( int id ) {
			assertEquals( 123, id );
			assertEquals( false, linked );
			linked = true;
		}

		@Override
		protected void bind( int id ) {
			if( bound ) {
				assertEquals( 0, id );
				bound = false;
			}
			else {
				assertEquals( 123, id );
				bound = true;
			}
		}

		@Override
		protected int getParameterCount() {
			return 2;
		}

		@Override
		protected ParameterDescriptor getShaderParameterDescriptor( int idx ) {
			switch( idx ) {
			case 0:
				return new ParameterDescriptor( "name", 2, "GL_FLOAT_VEC3", 456 );

			case 1:
				return new ParameterDescriptor( "gl_name", 2, "GL_FLOAT_VEC3", 456 );

			default:
				fail();
				return null;
			}
		}

		@Override
		public void setInteger( int loc, IntBuffer buffer ) {
			fail();
		}

		@Override
		public void setFloat( int loc, int size, FloatBuffer buffer ) {
			// Check args
			assertEquals( 456, loc );
			assertEquals( 3, size );

			// Check buffer
			assertNotNull( buffer );
			assertEquals( 6, buffer.capacity() );
			buffer.rewind();
			for( int n = 1; n <= 6; ++n ) {
				assertFloatEquals( n, buffer.get() );
			}

			// Check only invoked once
			assertEquals( false, updated );
			updated = true;
		}

		@Override
		public void setMatrix( int loc, int size, FloatBuffer buffer ) {
			fail();
		}

		@Override
		protected void delete( int id ) {
			assertEquals( 123, id );
			assertEquals( false, deleted );
			deleted = true;
		}
	}

	private MockShaderProgram program;
	private Shader shader;

	@Before
	public void before() {
		// Create a shader
		shader = mock( Shader.class );
		when( shader.getResourceID() ).thenReturn( 42 );

		// Create program
		program = new MockShaderProgram( new Shader[]{ shader } );
	}

	@Test
	public void constructor() {
		assertEquals( true, program.attached );
		assertEquals( true, program.linked );
		assertEquals( false, program.deleted );
		assertEquals( false, program.bound );
		assertEquals( false, program.updated );
		assertEquals( 123, program.getResourceID() );
		assertEquals( false, program.isInitialised() );
	}

	@Test
	public void getParameter() {
		// Lookup parameter
		final ShaderParameter p = program.getParameter( "name" );
		assertNotNull( p );
		assertEquals( "name", p.getName() );
		assertEquals( ParameterType.FLOAT, p.getType() );
		assertEquals( false, program.updated );

		// Update parameter
		p.set( new float[]{ 1, 2, 3, 4, 5, 6 }, program );
		assertEquals( true, program.updated );
	}

	@Test
	public void getParameterGlobalIgnored() {
		assertEquals( null, program.getParameter( "gl_name" ) );
	}

	@Test
	public void activate() {
		program.activate();
		assertEquals( true, program.bound );
	}

	@Test
	public void reset() {
		program.activate();
		program.reset();
		assertEquals( false, program.bound );
	}

	@Test
	public void release() {
		program.release();
		assertEquals( true, program.deleted );
	}

	@Test
	public void verify() {
		program.getParameter( "name" ).set( new float[]{ 1, 2, 3, 4, 5, 6 }, program );
		assertEquals( true, program.isInitialised() );
	}

	@Test
	public void verifyUnsetParameter() {
		assertEquals( false, program.isInitialised() );
	}
}
