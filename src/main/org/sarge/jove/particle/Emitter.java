package org.sarge.jove.particle;

import org.sarge.jove.geometry.Point;

/**
 * Emitter specification for a particle system.
 * @author Sarge
 */
public interface Emitter {
	/**
	 * @return Location of next particle
	 */
	Point emit();
}
