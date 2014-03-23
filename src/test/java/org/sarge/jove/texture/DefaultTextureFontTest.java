package org.sarge.jove.texture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.TextureCoord;

public class DefaultTextureFontTest {
	private DefaultTextureFont font;

	@Before
	public void before() {
		font = new DefaultTextureFont( 10, 1, 2 );
	}

	@Test
	public void getWidth() {
		assertEquals( 1, font.getWidth( 'w' ) );
	}

	@Test
	public void getHeight() {
		assertEquals( 2, font.getHeight() );
	}

	@Test
	public void getTextureCoords() {
		final TextureCoord[] coords = font.getTextureCoords( (char) 15 );
		assertNotNull( coords );
		assertEquals( 4, coords.length );
		assertEquals( new TextureCoord( 0.5f, 0.9f ), coords[ 0 ] );
		assertEquals( new TextureCoord( 0.5f, 0.8f ), coords[ 1 ] );
		assertEquals( new TextureCoord( 0.6f, 0.9f ), coords[ 2 ] );
		assertEquals( new TextureCoord( 0.6f, 0.8f ), coords[ 3 ] );
	}
}
