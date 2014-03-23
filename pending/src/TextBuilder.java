package org.sarge.jove.texture;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.MeshBuilder;
import org.sarge.jove.model.MeshLayout;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Quad;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Mesh builder for text using a {@link TextureFont}.
 * @author Sarge
 */
public class TextBuilder {
	// Mesh
	private final MeshBuilder builder;
	private final TextureFont font;

	// Text
	private float x, y;
	private float width = Float.MAX_VALUE;
	private Colour col = Colour.BLACK;
	private float scale = 1;

	/**
	 * Constructor.
	 * @param font Font properties
	 */
	public TextBuilder( TextureFont font ) {
		Check.notNull( font );
		this.font = font;
		builder = new MeshBuilder( MeshLayout.create( Primitive.TRIANGLES, "+VC0", false ) );
		builder.allocate( 256 ); // TODO
	}

	/**
	 * @return Mesh
	 */
	public MeshBuilder getMeshBuilder() {
		return builder;
	}

	/**
	 * @return Current horizontal position
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return Current vertical position
	 */
	public float getY() {
		return y;
	}

	/**
	 * Sets the text scaling factor.
	 * @param scale Text scale
	 */
	public void setScale( float scale ) {
		this.scale = scale;
	}

	/**
	 * Sets the maximum text width before enforced line-wrapping.
	 * @param width Maximum text width (default is unlimited)
	 */
	public void setWidth( float width ) {
		this.width = width;
	}

	/**
	 * Sets the current text colour.
	 * @param col Text colour
	 */
	public void setColour( Colour col ) {
		Check.notNull( col );
		this.col = col;
	}

	/**
	 * Adds some text.
	 * @param text Text to add
	 */
	public void append( String text ) {
		// Note current buffer position
		final int start = builder.getVertexCount();

		// Build glyphs
		for( char ch : text.toCharArray() ) {
			// Handle carriage returns
			if( ch == '\n' ) {
				newline();
				continue;
			}

			// Determine width of this character
			final float w = font.getWidth( ch );

			// Start newline if wrapped
			if( x + w > width ) {
				newline();
			}

			// Build quad for glyph
			final Quad quad = new Quad( new Point( x * scale, y * scale, 0 ), w * scale, font.getHeight() * scale, false );
			quad.setColour( col );
			quad.setTextureCoords( font.getTextureCoords( ch ) );

			// Add glyph
			builder.addQuad( quad.getVertices() );

			// Move to next glyph location
			x += w;
		}

		// Update mesh
		builder.build( start, builder.getVertexCount() );
		builder.flag=true;
	}

	/**
	 * Starts a new line of text.
	 */
	public void newline() {
		x = 0;
		y += font.getHeight();
	}

	/**
	 * Clears the text.
	 */
	public void clear() {
		// Clear mesh
		builder.reset();

		// Reset text position
		x = 0;
		y = 0;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
