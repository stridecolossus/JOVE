package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Plane.HalfSpace;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>bounding box</i> is an axis-aligned rectilinear volume implemented as an adapter for a {@link Bounds}.
 * @author Sarge
 */
public record BoundingBox(Bounds bounds) implements Volume {
	/**
	 * Constructor.
	 * @param bounds Bounds
	 */
	public BoundingBox {
		requireNonNull(bounds);
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
		final Vector normal = plane.normal();
		return intersects(plane, bounds.negative(normal)) || intersects(plane, bounds.positive(normal));
	}

	private static boolean intersects(Plane plane, Point p) {
		return plane.halfspace(p) != HalfSpace.NEGATIVE;
	}

	/**
	 * Determines ray intersections using the <i>component-wise slab</i> method.
	 * @see <a href="https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-box-intersection">Ray-box intersection</a>
	 */
	@Override
	public List<Intersection> intersections(Ray ray) {
		// Convert arguments to slab-wise components
		final float[] min = bounds.min().toArray();
		final float[] max = bounds.max().toArray();
		final float[] origin = ray.origin().toArray();
		final float[] dir = ray.direction().toArray();

		// Determine intersection distances
		float n = Float.NEGATIVE_INFINITY;
		float f = Float.POSITIVE_INFINITY;
		for(int c = 0; c < Vector.SIZE; ++c) {
			if(MathsUtility.isApproxZero(dir[c])) {
				// Check for parallel ray
				if((origin[c] < min[c]) || (origin[c] > max[c])) {
					return EMPTY_INTERSECTIONS;
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
					return EMPTY_INTERSECTIONS;
				}

				// Check for box behind ray
				if(f < 0) {
					return EMPTY_INTERSECTIONS;
				}
			}
		}

		// Build results
		final var far = new Intersection(f, this);
		if((n < 0) || MathsUtility.isApproxEqual(n, f)) {
			return List.of(far);
		}
		else {
			final var near = new Intersection(n, this);
			return List.of(near, far);
		}
	}

	@Override
	public Normal normal(Point intersection) {
		return IntersectedSurface.normal(bounds.centre(), intersection);
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
}
