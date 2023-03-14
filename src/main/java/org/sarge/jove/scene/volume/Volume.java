package org.sarge.jove.scene.volume;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;

/**
 * A <i>bounding volume</i> defines an abstract space for frustum culling, intersection tests and ray-picking.
 * @author Sarge
 */
public interface Volume extends Intersected {
	/**
	 * @return Bounds of this volume
	 */
	Bounds bounds();

	/**
	 * Tests whether the given point lies within this volume.
	 * @param p Point
	 * @return Whether this volume contains the given point
	 */
	boolean contains(Point p);

	/**
	 * Determines whether this volume intersects the given volume.
	 * <p>
	 * In general bounding volume intersections are assumed to degenerate to a test against a sphere or a {@link Bounds}.
	 * Implementations should perform class-specific intersection tests or delegate to the supplied volume.
	 * <p>
	 * Example implementation:
	 * <pre>
	 * class CustomVolume implements Volume {
	 *     public boolean intersects(Volume vol) {
	 *         return switch(vol) {
	 *             case SphereVolume sphere -> ...
	 *             case BoundingBox box -> ...
	 *             default -> vol.intersects(this);
	 *         }
	 *     }
	 * }
	 * </pre>
	 * <p>
	 * Note that this method throws an exception by default.
	 * <p>
	 * @param vol Volume
	 * @return Whether the volumes intersect
	 * @throws UnsupportedOperationException by default
	 */
	default boolean intersects(Volume vol) {
		throw new UnsupportedOperationException("Unsupported volumes: this=%s that=%s".formatted(this, vol));
	}

	/**
	 * Determines whether this volume is intersected by the given plane.
	 * @param plane Plane
	 * @return Whether intersected
	 */
	boolean intersects(Plane plane);
}
