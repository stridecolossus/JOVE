package org.sarge.jove.scene.volume;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.*;

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
		return Bounds.EMPTY;
	}

	@Override
	public boolean contains(Point p) {
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
	public Iterable<Intersection> intersections(Ray ray) {
		return Intersected.NONE;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
