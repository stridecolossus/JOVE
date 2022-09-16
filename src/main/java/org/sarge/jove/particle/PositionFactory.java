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

	/**
	 * Creates a position factory that generates random positions on a disc about the given origin.
	 * @param origin	Origin
	 * @param disc 		Disc
	 * @return Disc position factory
	 */
	static PositionFactory circle(Point origin, Disc disc) {
		return () -> origin.add(disc.point());
	}
}
