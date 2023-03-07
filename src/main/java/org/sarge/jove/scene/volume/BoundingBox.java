package org.sarge.jove.scene.volume;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.*;
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

	@Override
	public Bounds bounds() {
		return bounds;
	}

	@Override
	public boolean contains(Point p) {
		return bounds.contains(p);
	}

	@Override
	public boolean intersects(Volume vol) {
		return switch(vol) {
			case SphereVolume sphere -> sphere.intersects(bounds);
			case BoundingBox box -> bounds.intersects(box.bounds); // TODO - is this right? should check nearest < radius squared?
			default -> vol.intersects(this);
		};
	}

	@Override
	public boolean intersects(Plane plane) {
		final Vector n = plane.normal();
		return intersects(plane, bounds.negative(n)) || intersects(plane, bounds.positive(n));
	}

	private static boolean intersects(Plane plane, Point p) {
		return plane.halfspace(p) != HalfSpace.NEGATIVE;
	}

	/**
	 * Determines ray intersections using the component-wise slab method.
	 * @see <a href="https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection">Ray-box intersection</a>
	 */
	@Override
	public Iterable<Intersection> intersections(Ray ray) {
		// Convert arguments to slab-wise components
		final float[] min = bounds.min().toArray();
		final float[] max = bounds.max().toArray();
		final float[] origin = ray.origin().toArray();
		final float[] dir = ray.direction().toArray();

		// Determine intersection distances
		float n = Float.NEGATIVE_INFINITY;
		float f = Float.POSITIVE_INFINITY;
		for(int c = 0; c < Vector.SIZE; ++c) {
			if(MathsUtil.isZero(dir[c])) {
				// Check for parallel ray
				if((origin[c] < min[c]) || (origin[c] > max[c])) {
					return Intersected.NONE;
				}
			}
			else {
				// Calc intersection distances
				final float a = intersect(c, min, origin, dir);
				final float b = intersect(c, max, origin, dir);

				// Update intersections
				n = Math.max(n, Math.min(a, b));
				f = Math.min(f, Math.max(a, b));

				// Check for ray missing the box
				if(n > f) {
					return Intersected.NONE;
				}

				// Check for box behind ray
				if(f < 0) {
					return Intersected.NONE;
				}
			}
		}

		// Build results
		final Point centre = bounds.centre();
		final var far = Intersection.of(ray, f, centre);
		if((n < 0) || MathsUtil.isEqual(n, f)) {
			return List.of(far);
		}
		else {
			final var near = Intersection.of(ray, n, centre);
			return List.of(near, far);
		}
	}

	/**
	 * Calculates the intersection of the given ray component.
	 * @param index			Component index
	 * @param value			Ray
	 * @param origin		Origin
	 * @param dir			Direction
	 * @return Intersection distance
	 */
	private static float intersect(int index, float[] value, float[] origin, float[] dir) {
		return (value[index] - origin[index]) / dir[index];
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
