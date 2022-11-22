package org.sarge.jove.scene.volume;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>sphere volume</i> is a volume adapter for a {@link Sphere}.
 * @author Sarge
 */
public class SphereVolume implements Volume {
	/**
	 * Creates a sphere volume enclosing the given bounds.
	 * @param bounds Bounds
	 * @return Sphere volume
	 */
	public static SphereVolume of(Bounds bounds) {
		final Sphere sphere = new Sphere(bounds.centre(), bounds.largest() / 2f);
		return new SphereVolume(sphere);
	}

	private final Sphere sphere;

	/**
	 * Constructor.
	 * @param sphere Sphere
	 */
	public SphereVolume(Sphere sphere) {
		this.sphere = notNull(sphere);
	}

	/**
	 * @return Sphere
	 */
	public Sphere sphere() {
		return sphere;
	}

	/**
	 * @return Radius of this volume
	 */
	private float radius() {
		return sphere.radius();
	}

	@Override
	public Bounds bounds() {
		final float r = radius();
		final Point min = new Point(-r, -r, -r);
		final Point max = new Point(r, r, r);
		return new Bounds(min, max);
	}

	@Override
	public boolean contains(Point pt) {
		return contains(pt, radius());
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
		final Point centre = that.sphere.centre();
		final float r = this.radius() + that.radius();
		return contains(centre, r);
	}

	/**
	 * @return Whether this sphere contains the given point
	 */
	private boolean contains(Point p, float r) {
		return sphere.centre().distance(p) <= r * r;
	}

	@Override
	public boolean intersects(Plane plane) {
		final float d = plane.distance(sphere.centre());
		return Math.abs(d) <= radius();
	}

	/**
	 * Helper - Tests whether this sphere is intersected by the given bounds.
	 * @param bounds Bounds
	 * @return Whether intersected
	 */
	public boolean intersects(Bounds bounds) {
		final Point p = bounds.nearest(sphere.centre());
		return contains(p);
	}

	@Override
	public Intersection intersection(Ray ray) {
		// Determine length of the nearest point on the ray to the centre of the sphere
		final Vector vec = Vector.between(ray.origin(), sphere.centre());
		final float nearest = ray.direction().dot(vec);
		final float len = vec.magnitude();

		// Check case for sphere behind the ray origin
		if(nearest < 0) {
			return intersectBehind(ray, len, nearest);
		}

		// Calc distance of the nearest point from the sphere centre (using triangles)
		final float dist = nearest * nearest - len;

		// Stop if ray does not intersect
		final float r = radius();
		final float radius = r * r;
		if(Math.abs(dist) > radius) {
			return Intersection.NONE;
		}

		// Create lazy intersection record
		return new SphereIntersections() {
			@Override
			public float[] distances() {
				// Calculate offset from nearest point to intersection(s)
				// TODO - leave as squared?
				final float offset = MathsUtil.sqrt(radius - dist);

				// Build intersection results
				final float a = nearest + offset;
				if(len < radius) {
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
	private Intersection intersectBehind(Ray ray, float len, float nearest) {
		final float r = sphere.radius();
		final float radius = r * r;
		if(len > radius) {
			// Ray originates outside the sphere
			return Intersection.NONE;
		}
		else
		if(len < radius) {
			// Ray originates inside the sphere
			return new SphereIntersections() {
				@Override
				public float[] distances() {
					final float dist = len - nearest * nearest;
					final float offset = MathsUtil.sqrt(radius - dist);
					return new float[]{offset + nearest};
				}
			};
		}
		else {
			// Ray originates on the sphere surface
			return new SphereIntersections() {
				@Override
				public float[] distances() {
					return new float[]{0};
				}
			};
		}
	}

	/**
	 * Adapter for intersections with this sphere.
	 */
	private abstract class SphereIntersections implements Intersection {
		@Override
		public Normal normal(Point p) {
			return Vector.between(sphere.centre(), p).normalize();
		}
	}

	// https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection#bounding_spheres
	// http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
	// http://kylehalladay.com/blog/tutorial/math/2013/12/24/Ray-Sphere-Intersection.html

	@Override
	public int hashCode() {
		return sphere.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof SphereVolume that) &&
				this.sphere.equals(that.sphere);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(sphere).build();
	}
}