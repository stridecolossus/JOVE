package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;

/**
 * Generates the initial direction for new particles in a {@link ParticleSystem}.
 * @author Sarge
 */
public interface DirectionFactory {
	/**
	 * @return Direction of next particle
	 */
	Vector getDirection();
}
