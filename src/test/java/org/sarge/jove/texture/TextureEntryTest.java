package org.sarge.jove.texture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class TextureEntryTest {
	private TextureUnit entry;
	private Texture texture;

	@Before
	public void before() {
		texture = mock( Texture.class );
		entry = new TextureUnit( texture, 3 );
	}

	@Test
	public void constructor() {
		assertEquals( texture, entry.getTexture() );
		assertEquals( 3, entry.getTextureUnit() );
	}

	@Test
	public void equals() {
		assertEquals( true, entry.equals( entry ) );
		assertEquals( false, entry.equals( new TextureUnit( texture, 4 ) ) );
	}

	@Test
	public void activate() {
		entry.activate();
		verify( texture ).activate( 3 );

		entry.reset();
		verify( texture ).reset( 3 );
	}
}
