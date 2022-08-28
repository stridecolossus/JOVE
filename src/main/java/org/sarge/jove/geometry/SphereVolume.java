package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtil.*;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>sphere volume</i> is defined by a simple radius about a centre-point.
 * @author Sarge
 */
public class SphereVolume implements Volume {
	/**
	 * Creates a sphere volume enclosing the given bounds.
	 * @param bounds Bounds
	 * @return Sphere volume
	 */
	public static SphereVolume of(Bounds bounds) {
		return new SphereVolume(bounds.centre(), bounds.largest() / 2);
	}

	private final Point centre;
	private final float radius;

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public SphereVolume(Point centre, float radius) {
		this.centre = notNull(centre);
		this.radius = positive(radius);
	}

	/**
	 * @return Sphere centre
	 */
	public Point centre() {
		return centre;
	}

	/**
	 * @return Radius
	 */
	public float radius() {
		return radius;
	}

	@Override
	public boolean contains(Point pt) {
		return centre.distance(pt) <= radius * radius;
	}

	@Override
	public boolean intersects(Volume vol) {
		if(vol instanceof SphereVolume sphere) {
			final float r = radius + sphere.radius;
			return centre.distance(sphere.centre) <= r * r;
		}
		else {
			return vol.intersects(this);
		}
	}

	@Override
	public boolean intersects(Plane plane) {
		return Math.abs(plane.distance(centre)) <= radius;
	}

	/**
	 * Helper - Tests whether this sphere is intersected by the given bounds.
	 * @param bounds Bounds
	 * @return Whether intersected
	 */
	public boolean intersects(Bounds bounds) {
		final Point pt = bounds.nearest(centre);
		return contains(pt);
	}

	@Override
	public Iterator<Intersection> intersections(Ray ray) {
		// Determine length of the nearest point on the ray to the centre of the sphere
		final Vector vec = Vector.between(ray.origin(), centre);
		final float nearest = ray.direction().dot(vec);
		final float len = vec.magnitude();

		// Check case for sphere behind the ray origin
		if(nearest < 0) {
			return intersectBehind(ray, len, nearest);
		}

		// Calc distance of the nearest point from the sphere centre (using triangles)
		final float dist = nearest * nearest - len;

		// Stop if ray does not intersect
		final float r = radius * radius;
		if(Math.abs(dist) > r) {
			return Intersection.NONE;
		}

		// Create lazy intersection record
		return new SphereIntersectionIterator(ray) {
			@Override
			protected float[] intersections() {
				// Calculate offset from nearest point to intersection(s)
				final float offset = MathsUtil.sqrt(r - dist);

				// Build intersection results
				final float a = nearest + offset;
				if(len < r) {
					// Ray origin is inside the sphere
					return new float[]{a};
				}
				else {
					// Ray is outside the sphere (two intersections)
					final float b = nearest - offset;
					return new float[]{b, a};
				}
			}
		};
	}

	/**
	 * Determines intersections for the case where the sphere centre is <i>behind</i> the ray.
	 * @param ray			Intersection ray
	 * @param len			Distance from the ray origin to the centre of the sphere
	 * @param nearest		Length of the projected nearest point on the ray to the sphere centre
	 */
	private Iterator<Intersection> intersectBehind(Ray ray, float len, float nearest) {
		// Stop if ray is outside of the sphere
		final float r = radius * radius;
		if(len > r) {
			return Intersection.NONE;
		}

		// Otherwise calc intersection point
		if(len < r) {
			return new SphereIntersectionIterator(ray) {
				@Override
				protected float[] intersections() {
					final float dist = len - nearest * nearest;
					final float offset = MathsUtil.sqrt(r - dist);
					return new float[]{offset + nearest};
				}
			};
		}
		else {
			// Ray originates on the sphere surface
			return new SphereIntersectionIterator(ray) {
				@Override
				protected float[] intersections() {
					return new float[]{0};
				}
			};
		}
	}

	/**
	 * Lazily evaluated intersections iterator.
	 */
	private abstract class SphereIntersectionIterator implements Iterator<Intersection> {
		private final Ray ray;
		private float[] distances;
		private int index;

		protected SphereIntersectionIterator(Ray ray) {
			this.ray = notNull(ray);
		}

		/**
		 * @return Intersections
		 */
		protected abstract float[] intersections();

		/**
		 * Evaluates intersection distances.
		 */
		private void init() {
			if(distances == null) {
				distances = intersections();
			}
		}

		@Override
		public boolean hasNext() {
			init();
			return index < distances.length;
		}

		@Override
		public Intersection next() {
			// Get next intersection
			init();
			if(index >= distances.length) throw new NoSuchElementException();
			final float dist = distances[index++];

			// Build intersection with lazily evaluated surface normal
			return new Intersection(dist) {
				@Override
				public Vector normal() {
					final Vector vec = ray.direction().multiply(dist);
					final Point pt = ray.origin().add(vec);
					return Vector.between(centre, pt).normalize();
				}
			};
		}
	}

	/**
	 * Calculates the vector to the point on the unit-sphere for the given rotation angles (in radians).
	 * @param theta		Horizontal angle (or <i>yaw</i>) in the range zero to {@link MathsUtil#TWO_PI}
	 * @param phi		Vertical angle (or <i>pitch</i>) in the range +/- {@link MathsUtil#HALF_PI}
	 * @return Unit-sphere surface vector
	 */
	public static Vector vector(float theta, float phi) {
		// Apply 90 degree clockwise rotation to align with the -Z axis
		final float angle = theta - MathsUtil.HALF_PI;

		// Calculate unit-sphere coordinates
		final float cos = cos(phi);
		final float x = cos(angle) * cos;
		final float y = sin(angle) * cos;
		final float z = sin(phi);

		// Swizzle the coordinates to default space
		return new Vector(x, z, y);
	}

	// https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection#bounding_spheres
	// http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
	// http://kylehalladay.com/blog/tutorial/math/2013/12/24/Ray-Sphere-Intersection.html

	@Override
	public int hashCode() {
		return Objects.hash(centre, radius);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof SphereVolume that) &&
				this.centre.equals(that.centre) &&
				MathsUtil.isEqual(this.radius, that.radius);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(centre).append("r", radius).build();
	}
}
