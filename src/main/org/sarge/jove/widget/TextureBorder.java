package org.sarge.jove.widget;

import org.sarge.util.ToString;

/**
 * Border rendered as four texture strips.
 * The top/bottom sections are assumed to take precedence over the left/right sections.
 * TODO - diagram
 * @author Sarge
 */
public class TextureBorder implements Border {
	private final TextureDrawable[] border;
	
	public TextureBorder( TextureDrawable[] border ) {
		Check.notEmpty( border );
		if( border.length != 4 ) throw new IllegalArgumentException( "Expected 4 textures" );
		this.border = border.clone();
	}
	
	@Override
	public Dimensions getDimensions() {
		return null; // TODO
	}
	
	@Override
	public void render() {
		// TODO
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
