package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * An <i>inverse volume</i> is the space <i>outside</i> a given volume.
 * @author Sarge
 */
public record InverseVolume(Volume vol) implements Volume {
	/**
	 * Constructor.
	 * @param vol Volume
	 */
	public InverseVolume {
		requireNonNull(vol);
	}

	@Override
	public Bounds bounds() {
		return vol.bounds();
	}

	@Override
	public boolean contains(Point pt) {
		return !vol.contains(pt);
	}

	@Override
	public boolean intersects(Volume vol) {
		return !this.vol.intersects(vol);
	}

	@Override
	public boolean intersects(Plane plane) {
		return !vol.intersects(plane);
	}

	@Override
	public Iterable<Intersection> intersections(Ray ray) {
		return vol.intersections(ray);
	}

	@Override
	public int hashCode() {
		return -vol.hashCode();
	}
}
