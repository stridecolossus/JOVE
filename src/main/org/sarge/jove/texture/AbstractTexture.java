package org.sarge.jove.texture;

import org.sarge.jove.common.AbstractGraphicResource;
import org.sarge.lib.util.ToString;

/**
 * Template implementation for a texture.
 * @author Sarge
 */
public abstract class AbstractTexture extends AbstractGraphicResource implements Texture {
	@Override
	public void activate( int unit ) {
		bind( super.getResourceID(), unit );
	}

	@Override
	public void reset( int unit ) {
		bind( 0, unit );
	}

	/**
	 * Binds this texture.
	 * @param id		Texture ID
	 * @param unit		Texture unit
	 */
	protected abstract void bind( int id, int unit );

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
