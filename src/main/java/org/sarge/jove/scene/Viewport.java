package org.sarge.jove.scene;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Rectangle;

/**
 * Viewport.
 * @author Sarge
 */
public interface Viewport {
	/**
	 * Initialises the dimensions of this viewport.
	 * @param rect Viewport rectangle
	 */
	void init( Rectangle rect );

	/**
	 * Clears the frame buffer.
	 * @param col Clear colour
	 */
	void clear( Colour col );

	/**
	 * Clears the depth buffer.
	 */
	void clear();
}
