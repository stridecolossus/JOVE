package org.sarge.jove.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.shader.Shader;
import org.sarge.jove.shader.ShaderLoader;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureLoader;
import org.sarge.lib.io.ClasspathDataSource;

public class MaterialLoaderTest {
	private MaterialLoader loader;
	private ShaderLoader shaderLoader;
	private TextureLoader textureLoader;
	private RenderingSystem sys;

	@Before
	public void before() {
		shaderLoader = mock( ShaderLoader.class );
		textureLoader = mock( TextureLoader.class );
		sys = mock( RenderingSystem.class );
		loader = new MaterialLoader( new ClasspathDataSource( MaterialLoaderTest.class ), shaderLoader, textureLoader, sys );
	}

	@Test
	public void load() throws Exception {
		// Mock textures
		final Texture tex = mock( Texture.class );
		when( textureLoader.load( "texture.jpg" ) ).thenReturn( tex );
		when( textureLoader.load( "normal.jpg" ) ).thenReturn( tex );

		// Load material
		final Material mat = loader.load( "material.xml" );
		assertNotNull( mat );
		assertEquals( "test", mat.getName() );

		// Check shader created
		verify( shaderLoader ).load( new String[]{ "demo.vert", "demo.frag" } );
		verify( sys ).createShader( Shader.Type.VERTEX, null );
		verify( sys ).createShader( Shader.Type.FRAGMENT, null );
		verify( sys ).createShaderProgram( new Shader[]{ null, null } );
//
//		// Check colours
//		assertEquals( new Colour( 0.1f, 0.1f, 0.1f, 0.1f ), mat.getColour( "ambient" ) );
//		assertEquals( new Colour( 0.2f, 0.2f, 0.2f, 0.2f ), mat.getColour( "diffuse" ) );
//		assertEquals( new Colour( 0.3f, 0.3f, 0.3f, 0.3f ), mat.getColour( "specular" ) );
//		assertEquals( new Colour( 0.4f, 0.4f, 0.4f, 0.4f ), mat.getColour( "emissive" ) );
//
//		// Check shininess
//		assertFloatEquals( 42, mat.getFloat( "shininess" ) );
//
//		// Check textures
//		assertEquals( 2, mat.getTextures().size() );
//		assertEquals( 0, mat.getTexture( "texture" ).getTextureUnit() );
//		assertEquals( tex, mat.getTexture( "texture" ).getTexture() );
//		assertEquals( 1, mat.getTexture( "normal" ).getTextureUnit() );
//		assertEquals( tex, mat.getTexture( "normal" ).getTexture() );

		//
	}
}
