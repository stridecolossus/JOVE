package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;

/**
 * Factory for particle colours.
 * @author Sarge
 */
public interface ColourFactory {
	/**
	 * @return Colour of next particle
	 */
	Colour getColour();
}
