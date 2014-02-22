package org.sarge.jove.particle;

import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.RandomUtil;
import org.sarge.lib.util.Check;

/**
 * Particle emitter defined by an axis-aligned bounding box.
 * @author Sarge
 */
public class BoundingBoxEmitter implements Emitter {
	private final BoundingBox box;

	/**
	 * Constructor.
	 * @param box Bounding box
	 */
	public BoundingBoxEmitter( BoundingBox box ) {
		Check.notNull( box );
		this.box = box;
	}

	@Override
	public Point emit() {
		final Point min = box.getMin();
		final Point max = box.getMax();
		return new Point(
			RandomUtil.nextFloat( min.getX(), max.getX() ),
			RandomUtil.nextFloat( min.getY(), max.getY() ),
			RandomUtil.nextFloat( min.getZ(), max.getZ() )
		);
	}

	@Override
	public String toString() {
		return box.toString();
	}
}
