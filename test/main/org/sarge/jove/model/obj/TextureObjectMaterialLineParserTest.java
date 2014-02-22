package org.sarge.jove.model.obj;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.material.MutableMaterial;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureLoader;

public class TextureObjectMaterialLineParserTest {
	private TextureObjectMaterialLineParser parser;
	private TextureLoader loader;
	private MutableMaterial mat;

	@Before
	public void before() {
		loader = mock( TextureLoader.class );
		parser = new TextureObjectMaterialLineParser( "tex", loader );
		mat = mock( MutableMaterial.class );
	}

	@Test
	public void test() throws IOException {
		final Texture tex = mock( Texture.class );
		when( loader.load( "path" ) ).thenReturn( tex );
		parser.parse( new String[]{ "path" }, mat );
		verify( mat ).set( "tex", tex );
	}
}
