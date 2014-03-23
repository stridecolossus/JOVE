package org.sarge.jove.texture;

import org.sarge.jove.common.GraphicResource;

/**
 * Texture resource.
 * @author Sarge
 */
public interface Texture extends GraphicResource {
	/**
	 * Selects this texture.
	 * @param unit Texture unit
	 */
	void activate( int unit );

	/**
	 * Selects the default texture.
	 * @param unit Texture unit
	 */
	void reset( int unit );
}
