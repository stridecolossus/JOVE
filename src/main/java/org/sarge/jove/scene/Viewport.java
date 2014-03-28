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
	 * Clears viewport buffers.
	 * @param col Clear colour for frame buffer or <tt>null</tt> to skip
	 */
	void clear( Colour col );
}
