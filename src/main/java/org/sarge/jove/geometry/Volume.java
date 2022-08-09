package org.sarge.jove.geometry;

import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>bounding volume</i> defines an abstract volume of space used for frustum culling, intersection tests and ray-picking.
 * @author Sarge
 */
public interface Volume {
	/**
	 * Tests whether the given point lies within this volume.
	 * @param pt Point
	 * @return Whether this volume contains the given point
	 */
	boolean contains(Point pt);

	/**
	 * Determines whether this volume intersects the given volume.
	 * <p>
	 * In general a bounding volume intersection is assumed to ultimately degenerate to a test against a sphere or a {@link Bounds}.
	 * <br>
	 * Implementations should perform class-specific intersection tests or delegate to the supplied volume.
	 * <br>
	 * Note that {@link #intersects(Volume)} throws an exception by default.
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
	 * Determines the intersection(s) of this volume and the given ray.
	 * @param ray Ray
	 * @return Intersections
	 */
	Intersection intersect(Ray ray);

	/**
	 * Empty bounding volume.
	 */
	Volume EMPTY = new Volume() {
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
		public Intersection intersect(Ray ray) {
			return Intersection.NONE;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}

		@Override
		public String toString() {
			return "Empty";
		}
	};
}
