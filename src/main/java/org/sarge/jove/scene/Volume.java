package org.sarge.jove.scene;

import java.util.Optional;

import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;

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
	 * Determines the intersection of this volume and given ray
	 * @param ray Ray
	 * @return Intersection
	 * TODO - introduce lazy evaluated intersection point, i.e. this just returns y/n with method to then determine actual intersection point
	 */
	Optional<Point> intersect(Ray ray);

	/**
	 * Determines whether this volume intersects the given volume.
	 * @param vol Volume
	 * @return Whether the volumes intersects
	 * @throws UnsupportedOperationException by default
	 */
	default boolean intersects(Volume vol) {
		throw new UnsupportedOperationException(String.format("Unsupported volumes: this=%s that=%s", this, vol));
	}

// TODO - ?
//	interface Intersection {
//		Optional<Point> intersection();
// yes | no | touching
//	}

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
		public Optional<Point> intersect(Ray ray) {
			return Optional.empty();
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
