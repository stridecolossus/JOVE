package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.geometry.Tuple;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>bounding box</i> is an axis-aligned rectangular volume implemented as an adapter of an {@link Extents}.
 * @author Sarge
 */
public class BoundingBox implements Volume {
	private final Extents extents;

	/**
	 * Constructor given extents.
	 * @param extents Extents
	 */
	public BoundingBox(Extents extents) {
		this.extents = notNull(extents);
	}

	/**
	 * @return Extents of this bounding box
	 */
	public Extents extents() {
		return extents;
	}

	@Override
	public boolean contains(Point pt) {
		return extents.contains(pt);
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
			return vol.intersects(this);
		}
	}

	@Override
	public boolean intersects(Plane plane) {
		final Vector normal = plane.normal();
		final Point neg = extents.negative(normal);
		if(plane.halfspace(neg) == HalfSpace.NEGATIVE) {
			return false;
		}

		final Point pos = extents.positive(normal);
		if(plane.halfspace(pos) == HalfSpace.NEGATIVE) {
			return false;
		}

		return true;
	}

	@Override
	public Intersection intersect(Ray ray) {
		// Init intersections
		float near = Float.NEGATIVE_INFINITY;
		float far = Float.POSITIVE_INFINITY;

		// Test intersection on each pair of planes
		for(int n = 0; n < Tuple.SIZE; ++n) {
			// Tests are performed component-wise
			final float origin = ray.origin().get(n);
			final float dir = ray.direction().get(n);
			final float min = extents.min().get(n);
			final float max = extents.max().get(n);

			if(MathsUtil.isZero(dir)) {
				// Stop if parallel ray misses the box
				if((origin < min) || (origin > max)) {
					return Intersection.NONE;
				}
			}
			else {
				// Calc intersection points
				final float a = (min - origin) / dir;
				final float b = (max - origin) / dir;

				// Update near/far distances
				near = Math.max(near, Math.min(a, b));
				far = Math.min(far, Math.max(a, b));

				// Ray does not intersect
				if(near > far) {
					return Intersection.NONE;
				}

				// Volume is behind the ray
				if(far < 0) {
					return Intersection.NONE;
				}
			}
		}

		// Ray intersects twice
		return Intersection.of(near, far);
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
