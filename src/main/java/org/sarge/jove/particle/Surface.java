package org.sarge.jove.particle;

import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.scene.Volume;

/**
 * Collision surface.
 * @author Sarge
 */
@FunctionalInterface
public interface Surface {
	/**
	 * Action on particles that intersect this surface.
	 * @author Sarge
	 */
	enum Action {
		/**
		 * Particle is destroyed.
		 */
		DESTROY,

		/**
		 * Particle stops, i.e. sticks to the surface.
		 * @see Particle#stop()
		 */
		STOP,

		/**
		 * Particle is reflected.
		 */
		REFLECT
	}

	/**
	 * Tests whether a particle intersects this surface.
	 * @param pos Particle position
	 * @return Whether intersects
	 */
	boolean intersects(Point pos);

	/**
	 * Creates a collision surface defined by a plane.
	 * Particles that are not in <i>front</i> of the plane are considered as intersecting.
	 * @param plane Plane
	 * @return Plane collision surface
	 * @see Plane#side(Point)
	 */
	static Surface plane(Plane plane) {
		return pos -> plane.side(pos) != Plane.Side.FRONT;
	}

	/**
	 * Creates a collision surface from a bounding volume.
	 * @param vol Bounding volume
	 * @return Bounding volume surface
	 */
	static Surface volume(Volume vol) {
		// TODO
		// return pos -> vol.intersect(null);
		return pos -> false;
	}
}
