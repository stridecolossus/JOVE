package org.sarge.jove.particle;

import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Bounding box surface.
 * @author Sarge
 */
public class BoundingBoxCollisionSurface implements CollisionSurface {
	private final BoundingBox box;

	/**
	 * Constructor.
	 * @param box Bounding box
	 */
	public BoundingBoxCollisionSurface(BoundingBox box) {
		Check.notNull(box);
		this.box = box;
	}

	@Override
	public boolean intersects(Particle p) {
		return box.contains(p.getPosition());
	}

	@Override
	public Vector reflect(Vector vec) {
		// TODO - really needs to be against each side of the box
		return vec.invert();
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
