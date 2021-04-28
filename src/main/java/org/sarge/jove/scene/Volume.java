package org.sarge.jove.scene;

import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>bounding volume</i> defines an abstract volume of space used for frustum clipping and intersection tests.
 * @author Sarge
 */
public interface Volume {
	/**
	 * @return Extents of this volume
	 */
	Extents extents();

	/**
	 * @param pt Point
	 * @return Whether this volume contains the given point
	 */
	boolean contains(Point pt);

	/**
	 * Determines whether this volume intersects the given volume.
	 * @param vol Volume
	 * @return Whether the volumes intersects
	 * @throws UnsupportedOperationException by default
	 */
	default boolean intersects(Volume vol) {
		throw new UnsupportedOperationException(String.format("Unsupported volumes: this=%s that=%s", this, vol));
	}

	/**
	 * Determines the intersection(s) of this volume and the given ray.
	 * @param ray Ray
	 * @return Intersections
	 */
	Intersection intersect(Ray ray);

	/**
	 * Empty bounding volume.
	 */
	Volume NULL = new Volume() {
		@Override
		public boolean contains(Point pt) {
			return false;
		}

		@Override
		public Extents extents() {
			return new Extents(Point.ORIGIN, Point.ORIGIN);
		}

		@Override
		public Intersection intersect(Ray ray) {
			return Intersection.NONE;
		}

		@Override
		public boolean intersects(Volume vol) {
			return false;
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
