package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Ray.*;

/**
 * A <i>bounding volume</i> defines an abstract space used for frustum culling, intersection tests and ray-picking.
 * @author Sarge
 */
public interface Volume extends Intersected {
	/**
	 * @return Bounds of this volume
	 */
	Bounds bounds();

	/**
	 * Tests whether the given point lies within this volume.
	 * @param pt Point
	 * @return Whether this volume contains the given point
	 */
	boolean contains(Point pt);

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
	 *         if(obj instanceof SphereVolume sphere) {
	 *             return ...
	 *         }
	 *         else {
	 *             return vol.intersects(this);
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
		throw new UnsupportedOperationException(String.format("Unsupported volumes: this=%s that=%s", this, vol));
	}

	/**
	 * Determines whether this volume is intersected by the given plane.
	 * @param plane Plane
	 * @return Whether intersected
	 */
	boolean intersects(Plane plane);

	/**
	 * Empty bounding volume.
	 */
	Volume EMPTY = new Volume() {
		@Override
		public Bounds bounds() {
			return new Bounds(Point.ORIGIN, Point.ORIGIN);
		}

		@Override
		public boolean contains(Point pt) {
			return false;
		}

		@Override
		public boolean intersects(Volume vol) {
			return false;
		}

		@Override
		public boolean intersects(Plane plane) {
			return false;
		}

		@Override
		public Intersection intersection(Ray ray) {
			return Intersection.NONE;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
	};
}
