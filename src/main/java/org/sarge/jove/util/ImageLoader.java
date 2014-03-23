package org.sarge.jove.util;

import java.io.IOException;

/**
 * Loader for texture images.
 * @author Sarge
 */
public interface ImageLoader extends Loader<TextureImage> {
	/**
	 * @param flip Whether to flip images vertically (default is <tt>true</tt>)
	 */
	void setFlip( boolean flip );

	/**
	 * Loads an image stored in OpenGL RGB or RGBA format.
	 * @param path Image path
	 * @return Texture image
	 * @throws IOException if the file is not found or the image cannot be loaded
	 */
	@Override
	TextureImage load( String path ) throws IOException;
}
