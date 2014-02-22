package org.sarge.jove.texture;

import org.sarge.jove.common.TextureCoord;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mono-spaced texture font.
 * @author Sarge
 */
public class DefaultTextureFont implements TextureFont {
	private final int num;
	private final float size;
	private final int w, h;

	/**
	 * Constructor.
	 * @param num		Number of glyphs per row/column (assumes square texture)
	 * @param w			Character width
	 * @param h			Line height
	 */
	public DefaultTextureFont( int num, int w, int h ) {
		Check.oneOrMore( num );

		this.num = num;
		this.size = 1f / num;
		this.w = w;
		this.h = h;
	}

	@Override
	public int getWidth( char ch ) {
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public TextureCoord[] getTextureCoords( char ch ) {
		// Calc top-left and bottom-right coordinates
		final float x = ( ch % num ) * size;
		final float y = 1f - ( ( ch / num ) * size );
		final float dx = x + size;
		final float dy = y - size;

		// Build coords in triangle order (top-left, bottom-right)
		final TextureCoord[] coords = new TextureCoord[ 4 ];
		coords[ 0 ] = new TextureCoord(  x,  y );
		coords[ 1 ] = new TextureCoord(  x, dy );
		coords[ 2 ] = new TextureCoord( dx,  y );
		coords[ 3 ] = new TextureCoord( dx, dy );

		return coords;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
