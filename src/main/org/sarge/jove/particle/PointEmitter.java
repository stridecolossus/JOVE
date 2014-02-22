package org.sarge.jove.particle;

import org.sarge.jove.geometry.Point;
import org.sarge.lib.util.Check;

/**
 * Emits particles from a single location.
 * @author Sarge
 */
public class PointEmitter implements Emitter {
	private final Point pos;

	/**
	 * Constructor.
	 * @param pos Emission point
	 */
	public PointEmitter( Point pos ) {
		Check.notNull( pos );
		this.pos = pos;
	}

	/**
	 * Default constructor for {@link Point#ORIGIN} emitter.
	 */
	public PointEmitter() {
		this( Point.ORIGIN );
	}

	@Override
	public Point emit() {
		return pos;
	}

	@Override
	public String toString() {
		return pos.toString();
	}
}
