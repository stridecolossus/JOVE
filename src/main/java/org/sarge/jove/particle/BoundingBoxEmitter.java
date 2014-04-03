package org.sarge.jove.particle;

import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Particle emitter defined by an axis-aligned bounding box.
 * @author Sarge
 */
public class BoundingBoxEmitter implements Emitter {
	private final Point min, max;

	/**
	 * Constructor.
	 * @param box Bounding box
	 */
	public BoundingBoxEmitter( BoundingBox box ) {
		Check.notNull( box );
		this.min = box.getMinimum();
		this.max = box.getMaximum();
	}

	@Override
	public Point emit() {
		return new Point(
			MathsUtil.nextFloat( min.getX(), max.getX() ),
			MathsUtil.nextFloat( min.getY(), max.getY() ),
			MathsUtil.nextFloat( min.getZ(), max.getZ() )
		);
	}

	@Override
	public String toString() {
		return min.toString() + "/" + max.toString();
	}
}
