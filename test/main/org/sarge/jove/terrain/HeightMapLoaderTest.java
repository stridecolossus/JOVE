package org.sarge.jove.terrain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.util.ImageLoader;

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
		// Create a gray-scale image
		final BufferedImage image = new BufferedImage( 2, 3, BufferedImage.TYPE_BYTE_GRAY );
		when( imageLoader.load( null ) ).thenReturn( image );

		// Populate some height data
		for( int x = 0; x < 2; ++x ) {
			for( int y = 0; y < 3; ++y ) {
				final int h = x + y;
				image.setRGB( x, y, new Color( h, h, h ).getRGB() );
				//System.out.println(new Color( h, h, h ).getRGB() );
			}
		}

		// Load height-map
		final HeightMap map = loader.load( null );
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
