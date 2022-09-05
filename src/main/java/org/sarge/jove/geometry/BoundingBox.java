package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

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

	/**
	 * Determines ray intersections using the component-wise slab method.
	 * @see <a href="https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection">Ray-box intersection</a>
	 */
	@Override
	public Intersection intersection(Ray ray) {
		// Determine intersection distances
		float n = Float.NEGATIVE_INFINITY;
		float f = Float.POSITIVE_INFINITY;
		for(int c = 0; c < Vector.SIZE; ++c) {
			final float origin = ray.origin().get(c);
			final float dir = ray.direction().get(c);
			final float min = bounds.min().get(c);
			final float max = bounds.max().get(c);
			if(MathsUtil.isZero(dir)) {
				// Check for parallel ray
				if((origin < min) || (origin > max)) {
					return NONE;
				}
			}
			else {
				// Calc intersection distances
				final float a = intersect(min, origin, dir);
				final float b = intersect(max, origin, dir);

				// Update intersections
				n = Math.max(n, Math.min(a, b));
				f = Math.min(f, Math.max(a, b));

				// Check for ray missing the box
				if(n > f) {
					return NONE;
				}

				// Check for box behind ray
				if(f < 0) {
					return NONE;
				}
			}
		}

		// Build results
		final List<Float> distances = distances(n, f);
		return new Intersection() {
			@Override
			public List<Float> distances() {
				return distances;
			}

			@Override
			public Vector normal(Point p) {
				final Point pos = p.subtract(bounds.centre());
				return new Vector(pos).normalize();
			}
		};
	}

	/**
	 * Calculates the intersection of the given ray component.
	 * @param value			Ray component
	 * @param origin		Origin
	 * @param dir			Direction
	 * @return Intersection distance
	 */
	private static float intersect(float value, float origin, float dir) {
		return (value - origin) / dir;
	}

	/**
	 * Builds intersection distances.
	 */
	private static List<Float> distances(float n, float f) {
		if((n < 0) || MathsUtil.isEqual(n, f)) {
			return List.of(f);
		}
		else {
			return List.of(n, f);
		}
	}

	@Override
	public int hashCode() {
		return bounds.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof BoundingBox that) &&
				this.bounds.equals(that.bounds);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(bounds).build();
	}
}
