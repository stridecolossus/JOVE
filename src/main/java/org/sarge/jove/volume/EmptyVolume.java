package org.sarge.jove.volume;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * Null or empty volume.
 * @author Sarge
 */
public final class EmptyVolume implements Volume {
	/**
	 * Empty volume.
	 */
	public static final EmptyVolume INSTANCE = new EmptyVolume();

	private EmptyVolume() {
	}

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
}
