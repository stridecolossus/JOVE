package org.sarge.jove.scene.volume;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * An <i>inverse volume</i> is the space <i>outside</i> a given volume.
 * @author Sarge
 */
public class InverseVolume implements Volume {
	private final Volume vol;

	/**
	 * Constructor.
	 * @param vol Volume
	 */
	public InverseVolume(Volume vol) {
		this.vol = notNull(vol);
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
		return -Objects.hash(vol);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof InverseVolume that) && this.vol.equals(that.vol);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(vol).build();
	}
}
