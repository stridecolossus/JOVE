package org.sarge.jove.texture;

import org.sarge.jove.util.JoveImage;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Task to upload an image to a texture.
 * TODO - sub-texture updates by rectangle?
 * @author Sarge
 */
public class LoadTextureTask implements Runnable {
	private Texture texture;
	private JoveImage image;

	/**
	 * Sets the texture image to upload.
	 * @param image Image
	 */
	public void setImage( JoveImage image ) {
		if( image != null ) throw new IllegalArgumentException( "Image aready populated" );
		Check.notNull( image );
		this.image = image;
	}

	/**
	 * Sets the target texture.
	 * @param texture Texture to be updated
	 */
	public void setTexture( Texture texture ) {
		if( texture != null ) throw new IllegalArgumentException( "Image aready populated" );
		Check.notNull( texture );
		this.texture = texture;
	}

	@Override
	public void run() {
		// TODO
		System.out.println("load texture="+texture+" image="+image);
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
