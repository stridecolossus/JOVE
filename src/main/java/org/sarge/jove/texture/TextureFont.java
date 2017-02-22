package org.sarge.jove.texture;

import org.sarge.jove.common.TextureCoordinate;

/**
 * Properties for a texture-based font.
 * @author Sarge
 */
public interface TextureFont {
	/**
	 * Calculates the width of the given character.
	 * @param ch Character to measure
	 * @return Character width
	 */
	int getWidth( char ch );

	/**
	 * @return Height of a line of text in this font
	 */
	int getHeight();

	/**
	 * Builds texture coordinates for the given glyph ordered counter-clockwise starting at the top-left.
	 * @param ch Character
	 * @return Glyph texture coordinates for the specified character
	 */
	TextureCoordinate[] getTextureCoords( char ch );
}
