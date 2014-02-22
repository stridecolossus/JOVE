package org.sarge.jove.terrain;

import java.awt.image.BufferedImage;

public class TextureBlender {
	public BufferedImage blend( HeightMap map, WeightsFactory factory, BufferedImage[] textures ) {
		// Create blended image
		final int w = map.getWidth();
		final int h = map.getHeight();
		final BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );		

		// Blend textures
		final float[] weights = new float[ textures.length ];
		for( int x = 0; x < w; ++x ) {
			for( int y = 0; y < h; ++y ) {
				// Get weights for this vertex
				final float height = map.getHeight( x, y );
				factory.getWeights( height, weights );

				// Calculate colour contribution from each texture
				int total = 0;
				for( int n = 0; n < textures.length; ++n ) {
					final int col = textures[ n ].getRGB( x, y );
					final int scaled = scale( col, weights[ n ] );
					total += scaled;
				}
				
				// Write final colour to image
				image.setRGB( x, y, total );
			}
		}
		
		return image;
	}
	
	private static int scale( int col, float weight ) {
		final float a = ( col >> 24 ) & 0x000000FF;
		final float r = ( col >> 16 ) & 0x000000FF;
		final float g = ( col >> 8 ) & 0x000000FF;
		final float b = ( col ) & 0x000000FF;
		return (int) ( a * weight + r * weight + g * weight + b * weight );
	}
}
