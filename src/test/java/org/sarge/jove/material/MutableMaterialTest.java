package org.sarge.jove.material;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderParameter;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureUnit;

public class MutableMaterialTest {
	private MaterialBuilder mat;
	private ShaderProgram shader;
	private ShaderParameter param;
	private RenderContext ctx;

	@Before
	public void before() {
		mat = new MaterialBuilder( "test" );
		ctx = mock( RenderContext.class );
		shader = mock( ShaderProgram.class );
		param = mock( ShaderParameter.class );
		mat.setShader( shader );
	}

	@Test
	public void constructor() {
		assertEquals( "test", mat.getName() );
		assertEquals( true, mat.getRenderProperties().isEmpty() );
		assertEquals( shader, mat.getShader() );
	}

	@Test
	public void lifecycle() {
		// Apply material and shader
		mat.apply( ctx );
		verify( shader ).activate();

		// Reset material and shader
		mat.reset( ctx );
		verify( shader ).reset();
	}

	@Test
	public void setIntegerMaterialProperty() {
		when( shader.getParameter( "g_time" ) ).thenReturn( param );
		mat.set( "time", MaterialProperty.ELAPSED_TIME );
		mat.apply( ctx );
		verify( shader ).getParameter( "g_time" );
		verify( param ).set( ctx.getElapsed(), shader );
	}

	@Test
	public void setMatrixMaterialProperty() {
		when( shader.getParameter( "g_matrix" ) ).thenReturn( param );
		when( ctx.getModelMatrix() ).thenReturn( Matrix.IDENTITY );
		mat.set( "matrix", MaterialProperty.MODEL_MATRIX );
		mat.apply( ctx );
		verify( shader ).getParameter( "g_matrix" );
		verify( param ).set( Matrix.IDENTITY, shader );
	}

	@Test
	public void setRenderProperty() {
		// Mock rendering system
		final RenderingSystem sys = mock( RenderingSystem.class );
		when( ctx.getRenderingSystem() ).thenReturn( sys );

		// Add property
		final RenderProperty prop = mock( RenderProperty.class );
		when( prop.getType() ).thenReturn( "prop" );
		mat.add( prop );

		// Check added to material
		assertEquals( 1, mat.getRenderProperties().size() );
		assertEquals( prop, mat.getRenderProperties().get( "prop" ) );

		// Apply material and check property applied
		mat.apply( ctx );
		verify( prop ).apply( sys );
	}

	@Test
	public void setTextureEntry() {
		// Add a specific texture unit
		when( shader.getParameter( "m_tex" ) ).thenReturn( param );
		final Texture texture = mock( Texture.class );
		final TextureUnit entry = new TextureUnit( texture, 3 );
		mat.set( "tex", entry );

		// Apply material and check texture activated
		mat.apply( ctx );
		verify( texture ).activate( 3 );

		// Reset and check texture deactivated
		mat.reset( ctx );
		verify( texture ).reset( 3 );
	}

	@Test
	public void setTexture() {
		// Add next texture unit
		when( shader.getParameter( "m_tex" ) ).thenReturn( param );
		final Texture texture = mock( Texture.class );
		mat.set( "tex", texture );

		// Apply material and check texture activated
		mat.apply( ctx );
		verify( texture ).activate( 0 );

		// Reset and check texture deactivated
		mat.reset( ctx );
		verify( texture ).reset( 0 );
	}

	@Test(expected=IllegalArgumentException.class)
	public void setTextureEntryDuplicate() {
		final Texture texture = mock( Texture.class );
		mat.set( "one", new TextureUnit( texture, 3 ) );
		mat.set( "two", new TextureUnit( texture, 3 ) );
	}

	@Test
	public void setShaderParameter() {
		// Set a shader parameter
		when( shader.getParameter( "m_param" ) ).thenReturn( param );
		final Bufferable value = mock( Bufferable.class );
		mat.set( "param", value );

		// Apply material and check parameter updated
		mat.apply( ctx );
		verify( param ).set( value, shader );

		// Apply again and check unmodified
		mat.apply( ctx );
		verifyZeroInteractions( param );

		// Update value and check shader parameter updated
		mat.set( "param", value );
		mat.apply( ctx );
		verify( param, times( 2 ) ).set( value, shader );
	}

	@Test
	public void setFloatShaderParameter() {
		when( shader.getParameter( "m_param" ) ).thenReturn( param );
		mat.set( "param", 42f );
		mat.apply( ctx );
		verify( param ).set( 42f, shader );
	}

	@Test
	public void setIntegerShaderParameter() {
		when( shader.getParameter( "m_param" ) ).thenReturn( param );
		mat.set( "param", 42 );
		mat.apply( ctx );
		verify( param ).set( 42, shader );
	}

	@Test
	public void setBooleanShaderParameter() {
		when( shader.getParameter( "m_param" ) ).thenReturn( param );
		mat.set( "param", true );
		mat.apply( ctx );
		verify( param ).set( true, shader );
	}

/*
	@Test
	public void update() {
		// Add a frame-scope property
		final ShaderParameter time = mock( ShaderParameter.class );
		when( shader.getParameter( "g_time" ) ).thenReturn( time );
		mat.set( "time", MaterialProperty.ELAPSED_TIME );

		// Add a node-scope property
		when( ctx.getModelMatrix() ).thenReturn( Matrix.IDENTITY );
		when( shader.getParameter( "g_matrix" ) ).thenReturn( param );
		mat.set( "matrix", MaterialProperty.MODEL_MATRIX );

		// Update shader and check only node-scope properties are applied
		mat.update( ctx );
		verifyZeroInteractions( time );
		verify( param ).set( Matrix.IDENTITY, shader );
	}
*/
}
