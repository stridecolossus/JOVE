package org.sarge.jove.particle;

import org.sarge.jove.geometry.Point;

/**
 * A <i>position factory</i> generates the starting position of a {@link Particle}.
 * @author Sarge
 */
@FunctionalInterface
public interface PositionFactory {
	/**
	 * @return Particle position
	 */
	Point position();

	/**
	 * Origin factory.
	 */
	PositionFactory ORIGIN = of(Point.ORIGIN);

	/**
	 * Creates a position factory at the given point.
	 * @param pos Position
	 * @return Literal position factory
	 */
	static PositionFactory of(Point pos) {
		return () -> pos;
	}
}
