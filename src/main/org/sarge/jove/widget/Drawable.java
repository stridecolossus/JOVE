package org.sarge.jove.widget;

/**
 * Defines a 2D object such as an image or text.
 * @author Sarge
 */
public interface Drawable {
	/**
	 * @return Dimensions of this object
	 */
	Dimensions getDimensions();

	/**
	 * Renders this object.
	 */
	void render( Object obj );
}
