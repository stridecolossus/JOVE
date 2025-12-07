package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.*;
import org.sarge.jove.util.MathsUtility;

/**
 * Volume defined by a sphere.
 * @author Sarge
 */
public record SphereVolume(Point centre, float radius) implements Volume {
	/**
	 * Creates a sphere volume enclosing the given bounds.
	 * @param bounds Bounds
	 * @return Sphere volume
	 */
	public static SphereVolume of(Bounds bounds) {
		return new SphereVolume(bounds.centre(), bounds.largest() / 2);
	}

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 */
	public SphereVolume {
		requireNonNull(centre);
	}

	@Override
	public Bounds bounds() {
		final Point min = new Point(-radius, -radius, -radius);
		final Point max = new Point(radius, radius, radius);
		return new Bounds(min, max);
	}

	@Override
	public boolean contains(Point p) {
		return contains(p, radius);
	}

	@Override
	public boolean intersects(Volume vol) {
		return switch(vol) {
			case SphereVolume sphere -> intersects(sphere);
			case BoundingBox box -> intersects(box.bounds());
			default -> vol.intersects(this);
		};
	}

	/**
	 * @return Whether two sphere volumes intersect
	 */
	private boolean intersects(SphereVolume that) {
		final float r = this.radius + that.radius();
		return contains(that.centre, r);
	}

	/**
	 * @return Whether this sphere contains the given point
	 */
	private boolean contains(Point p, float r) {
		return centre.distance(p) <= r * r;
	}

	@Override
	public boolean intersects(Plane plane) {
		final float d = plane.distance(centre);
		return Math.abs(d) <= radius;
	}

	/**
	 * Helper.
	 * Tests whether this sphere is intersected by the given bounds.
	 * @param bounds Bounds
	 * @return Whether intersected
	 */
	public boolean intersects(Bounds bounds) {
		final Point p = bounds.nearest(centre);
		return contains(p);
	}

	@Override
	public Normal normal(Point intersection) {
		return IntersectedSurface.normal(centre, intersection);
	}

	@Override
	public List<Intersection> intersections(Ray ray) {
		// Determine distance to nearest intersection
		final Vector vec = Vector.between(ray.origin(), centre);
		final float nearest = ray.direction().dot(vec);
		final float distance = vec.magnitude();
		final float radiusSquared = radius * radius;

		// Init intersection helper
		final var builder = new Object() {
			/**
			 * Lazily evaluates intersections.
			 */
    		public List<Intersection> intersections() {
    			// Check for the case where the sphere is behind the ray
    			if(nearest < 0) {
    				return behind();
    			}

    			// Calc distance to the nearest point from the sphere centre using triangles
    			final float dist = nearest * nearest - distance;

    			// Stop if ray does not intersect
    			if(Math.abs(dist) > radiusSquared) {
    				return EMPTY_INTERSECTIONS;
    			}

				// Calculate offset from nearest point to intersection(s)
				// TODO - custom compare() to avoid sqrt?
				final float offset = MathsUtility.sqrt(radiusSquared - dist);

				// Build intersection results
				final Intersection a = new Intersection(nearest + offset, SphereVolume.this);
				if(distance < radiusSquared) {
					// Ray origin is inside the sphere
					return List.of(a);
				}
				else {
					// Ray is outside the sphere (two intersections)
					final Intersection b = new Intersection(nearest - offset, SphereVolume.this);
					return List.of(b, a);
				}
    		}

    		/**
    		 * Evaluates intersections for the case when the sphere is behind the ray.
    		 */
    		private List<Intersection> behind() {
    			if(distance > radiusSquared) {
    				// Ray originates outside the sphere
    				return EMPTY_INTERSECTIONS;
    			}
    			else
    			if(distance < radiusSquared) {
    				// Ray originates inside the sphere
    				final float dist = distance - nearest * nearest;
    				final float offset = MathsUtility.sqrt(radiusSquared - dist);
    				return List.of(new Intersection(offset + nearest, SphereVolume.this));
    			}
    			else {
    				// Ray originates on the sphere surface
    				return List.of(new Intersection(0, SphereVolume.this));
    			}
    		}
    	};

    	// Evaluate intersections
    	return builder.intersections();
	}
}
