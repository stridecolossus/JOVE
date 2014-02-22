package org.sarge.jove.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderParameter;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureEntry;

public class MutableMaterialTest {
	private MutableMaterial mat;
	private ShaderProgram shader;
	private RenderContext ctx;

	@Before
	public void before() {
		mat = new MutableMaterial( "test" );
		ctx = mock( RenderContext.class );
		shader = mock( ShaderProgram.class );
	}

	@Test
	public void constructor() {
		assertEquals( "test", mat.getName() );
		assertEquals( null, mat.getShader() );
		assertTrue( mat.getRenderProperties().isEmpty() );
	}

	@Test
	public void setShader() {
		// Set shader
		mat.setShader( shader );
		assertEquals( shader, mat.getShader() );

		// Apply material and check shader activated
		mat.apply( ctx );
		verify( shader ).activate();

		// Reset material and check deactivated
		mat.reset();
		verify( shader ).reset();
	}

	@Test
	public void setGlobalParameter() {
		// Add a global colour parameter
		final ShaderParameter param = mock( ShaderParameter.class );
		when( shader.getParameter( "m_col" ) ).thenReturn( param );
		mat.setShader( shader );

		// Set colour and check is applied to shader
		mat.set( "col", Colour.WHITE );
		mat.apply( ctx );
		verify( param ).setValue( Colour.WHITE );
	}

	@Test
	public void setMaterialProperty() {
		// Add a node-scope property
		final ShaderParameter param = mock( ShaderParameter.class );
		when( shader.getParameter( "g_matrix" ) ).thenReturn( param );
		mat.setShader( shader );

		// Mock model-matrix
		final Matrix matrix = new Matrix( 4 );
		when( ctx.getModelMatrix() ).thenReturn( matrix );

		// Apply material and check parameter initialised
		mat.set( "matrix", MaterialProperty.MODEL_MATRIX );
		mat.apply( ctx );
		verify( param ).setValue( matrix );

		// Init material for a new node and check parameter updated again
		mat.update( ctx );
		verify( param, times( 2 ) ).setValue( matrix );
	}

	@Test
	public void setTextureEntry() {
		// Add a texture parameter
		final ShaderParameter param = mock( ShaderParameter.class );
		when( shader.getParameter( "m_tex" ) ).thenReturn( param );
		mat.setShader( shader );

		// Set texture
		final Texture tex = mock( Texture.class );
		final TextureEntry entry = new TextureEntry( tex, 3 );
		mat.set( "tex", entry );

		// Apply material, check texture activated and parameter updated
		mat.apply( ctx );
		verify( tex ).activate( 3 );
		verify( param ).setValue( entry );
	}

	@Test
	public void setTexture() {
		// Add a texture parameter
		final ShaderParameter param = mock( ShaderParameter.class );
		when( shader.getParameter( "m_tex" ) ).thenReturn( param );
		mat.setShader( shader );

		// Set texture
		final Texture tex = mock( Texture.class );
		mat.set( "tex", tex );

		// Apply material, check texture activated and parameter updated
		mat.apply( ctx );
		verify( tex ).activate( 0 );
		verify( param ).setValue( new TextureEntry( tex, 0 ) );

		// Reset material and check texture deactivated
		mat.reset();
		verify( tex ).reset( 0 );
	}

	@Test
	public void addEffect() {
		// Add an effect
		final RenderProperty effect = mock( RenderProperty.class );
		when( effect.getType() ).thenReturn( "effect" );
		mat.add( effect );
		mat.getRenderProperties().containsValue( effect );

		// Apply material and check effect applied
		mat.apply( ctx );
		verify( effect ).apply( null );
	}

	@Test( expected = IllegalArgumentException.class )
	public void parameterWithoutShader() {
		mat.set( "param", new Object() );
		mat.setShader( null );
		mat.apply( ctx );
	}

	@Test( expected = IllegalArgumentException.class )
	public void unknownParameter() {
		mat.set( "param", new Object() );
		mat.setShader( shader );
		mat.apply( ctx );
	}
}
