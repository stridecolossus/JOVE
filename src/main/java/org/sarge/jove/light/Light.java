package org.sarge.jove.light;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * Light.
 * @author Sarge
 */
public interface Light {
	/**
	 * @return Type identifier (used to differentiate the different implementations in lighting shaders)
	 */
	int getType();

	/**
	 * @return Light colour
	 */
	Colour getColour();

	/**
	 * @return Light direction
	 */
	Vector getDirection();

	/**
	 * @return Light position
	 */
	Point getPosition();
}
