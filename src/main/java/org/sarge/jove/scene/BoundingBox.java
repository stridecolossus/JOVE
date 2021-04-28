package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>bounding box</i> is an axis-aligned rectangular volume implemented as an adapter of an {@link Extents}.
 * @author Sarge
 */
public final class BoundingBox implements Volume {
	private final Extents extents;

	/**
	 * Constructor given extents.
	 * @param extents Extents
	 */
	public BoundingBox(Extents extents) {
		this.extents = notNull(extents);
	}

	@Override
	public Extents extents() {
		return extents;
	}

	@Override
	public boolean contains(Point pt) {
		return extents.contains(pt);
	}

	@Override
	public Intersection intersect(Ray ray) {
		// TODO
		return null;
	}

	@Override
	public boolean intersects(Volume vol) {
		if(vol instanceof SphereVolume sphere) {
			return sphere.intersects(extents);
		}
		else
		if(vol instanceof BoundingBox box) {
			return extents.intersects(box.extents);
		}
		else {
			return extents.intersects(vol.extents());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(extents);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BoundingBox box) && this.extents.equals(box.extents);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(extents).build();
	}
}
