package org.sarge.jove.particle;

import org.sarge.jove.geometry.Point;

/**
 * Emitter specification for a particle system.
 * @author Sarge
 */
@FunctionalInterface
public interface Emitter {
	/**
	 * @return Location of next particle
	 */
	Point emit();

	/**
	 * @param pos Emission point
	 * @return Point emitter
	 */
	static Emitter point(Point pos) {
		return () -> pos;
	}

	/**
	 * Origin emitter.
	 */
	Emitter ORIGIN = point(Point.ORIGIN);
}
