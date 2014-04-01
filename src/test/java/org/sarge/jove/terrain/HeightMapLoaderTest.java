package org.sarge.jove.terrain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.ImageLoader;
import org.sarge.jove.util.JoveImage;

@Ignore
public class HeightMapLoaderTest {
	private HeightMapLoader loader;
	private ImageLoader imageLoader;

	@Before
	public void before() {
		imageLoader = mock( ImageLoader.class );
		loader = new HeightMapLoader( imageLoader );
	}

	@Test
	public void load() throws IOException {
		// Create gray-scale byte-buffer
		final ByteBuffer buffer = BufferFactory.createByteBuffer( 2 * 3 * 3 );
		for( int x = 0; x < 2; ++x ) {
			for( int y = 0; y < 3; ++y ) {
				final byte h = (byte) ( x + y );
				buffer.put( h );
				buffer.put( h );
				buffer.put( h );
			}
		}

		// Create a gray-scale image
		final JoveImage image = mock( JoveImage.class );
		when( image.hasAlpha() ).thenReturn( false );
		when( image.getBuffer() ).thenReturn( buffer );
		when( imageLoader.load( "path" ) ).thenReturn( image );

		// Load height-map
		final HeightMap map = loader.load( "path" );
		assertNotNull( map );

		// Verify height-map data
		assertEquals( 2, map.getWidth() );
		assertEquals( 3, map.getHeight() );
		for( int x = 0; x < 2; ++x ) {
			for( int y = 0; y < 3; ++y ) {
				assertFloatEquals( x + y, map.getHeight( x, y ) );
			}
		}
	}
}
