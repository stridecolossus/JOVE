package org.sarge.jove.scene.volume;

import java.util.List;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.*;

/**
 * Null or empty volume.
 * @author Sarge
 */
public record EmptyVolume() implements Volume {
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
	public List<Intersection> intersections(Ray ray) {
		return IntersectedSurface.EMPTY_INTERSECTIONS;
	}

	@Override
	public Normal normal(Point intersection) {
		throw new RuntimeException();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmptyVolume;
	}
}
