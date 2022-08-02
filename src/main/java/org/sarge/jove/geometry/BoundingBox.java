package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>bounding box</i> is an axis-aligned rectilinear volume implemented as an adapter for a {@link Bounds}.
 * @author Sarge
 */
public class BoundingBox implements Volume {
	private final Bounds bounds;

	/**
	 * Constructor.
	 * @param bounds Bounds
	 */
	public BoundingBox(Bounds bounds) {
		this.bounds = notNull(bounds);
	}

	/**
	 * @return Bounds of this volume
	 */
	public Bounds bounds() {
		return bounds;
	}

	@Override
	public boolean contains(Point pt) {
		return bounds.contains(pt);
	}

	@Override
	public boolean intersects(Volume vol) {
		return switch(vol) {
			case SphereVolume sphere -> sphere.intersects(bounds);
			case BoundingBox box -> bounds.intersects(box.bounds);
			default -> vol.intersects(this);
		};
	}

	@Override
	public boolean intersects(Plane plane) {
		final Vector normal = plane.normal();
		final Point neg = bounds.negative(normal);
		if(plane.halfspace(neg) == HalfSpace.NEGATIVE) {
			return false;
		}

		final Point pos = bounds.positive(normal);
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
		for(int n = 0; n < Vector.SIZE; ++n) {
			// Tests are performed component-wise
			final float origin = ray.origin().get(n);
			final float dir = ray.direction().get(n);
			final float min = bounds.min().get(n);
			final float max = bounds.max().get(n);

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
		return bounds.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this) || (obj instanceof BoundingBox that) && this.bounds.equals(that.bounds);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(bounds).build();
	}
}
