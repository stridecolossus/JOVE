package org.sarge.jove.scene.volume;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>sphere volume</i> is a bounding volume adapter for a {@link Sphere}.
 * @author Sarge
 */
public class SphereVolume implements Volume {
	/**
	 * Creates a sphere volume enclosing the given bounds.
	 * @param bounds Bounds
	 * @return Sphere volume
	 */
	public static SphereVolume of(Bounds bounds) {
		final Sphere sphere = new Sphere(bounds.centre(), bounds.largest() / 2);
		return new SphereVolume(sphere);
	}

	private final Sphere sphere;

	/**
	 * Constructor.
	 * @param sphere Sphere
	 */
	public SphereVolume(Sphere sphere) {
		this.sphere = requireNonNull(sphere);
	}

	/**
	 * @return Sphere
	 */
	public Sphere sphere() {
		return sphere;
	}

	@Override
	public Bounds bounds() {
		final float r = sphere.radius();
		final Point min = new Point(-r, -r, -r);
		final Point max = new Point(r, r, r);
		return new Bounds(min, max);
	}

	@Override
	public boolean contains(Point pt) {
		return contains(pt, sphere.radius());
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
		final float r = this.sphere.radius() + that.sphere.radius();
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
		return Math.abs(d) <= sphere.radius();
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
	public Iterable<Intersection> intersections(Ray ray) {
		// Determine distance to nearest intersection
		final Vector vec = Vector.between(ray.origin(), sphere.centre());
		final float nearest = ray.direction().dot(vec);
		final float distance = vec.magnitude();
		final float radiusSquared = sphere.radius() * sphere.radius();

		// Init intersection helper
		final var builder = new Object() {
			/**
			 * Lazily evaluates intersections.
			 */
    		public Iterable<Intersection> intersections() {
    			// Check for the case where the sphere is behind the ray
    			if(nearest < 0) {
    				return behind();
    			}

    			// Calc distance to the nearest point from the sphere centre using triangles
    			final float dist = nearest * nearest - distance;

    			// Stop if ray does not intersect
    			if(Math.abs(dist) > radiusSquared) {
    				// TODO - are we repeating?
    				return Intersection.NONE;
    			}

    			// Lazily evaluate intersections
    			return () -> {
    				// Calculate offset from nearest point to intersection(s)
    				// TODO - custom compare() to avoid sqrt
    				final float offset = MathsUtility.sqrt(radiusSquared - dist);

    				// Build intersection results
    				final Intersection a = ray.intersection(nearest + offset, sphere.centre());
    				if(distance < radiusSquared) {
    					// Ray origin is inside the sphere
    					return List.of(a).iterator();
    				}
    				else {
    					// Ray is outside the sphere (two intersections)
    					final Intersection b = ray.intersection(nearest - offset, sphere.centre());
    					return List.of(b, a).iterator();
    				}
    			};
    		}

    		/**
    		 * Evaluates intersections for the case when the sphere is behind the ray.
    		 */
    		private Iterable<Intersection> behind() {
    			if(distance > radiusSquared) {
    				// Ray originates outside the sphere
    				// TODO - are we repeating? (see above)
    				return Intersection.NONE;
    			}
    			else
    			if(distance < radiusSquared) {
    				// Ray originates inside the sphere
    				return List.of(inside());
    			}
    			else {
    				// Ray originates on the sphere surface
    				return List.of(ray.intersection(0, sphere.centre()));
    			}
    		}

    		/**
    		 * Creates an intersection for the case where the ray is inside the sphere.
    		 */
    		private Intersection inside() {
    			return ray.new AbstractIntersection() {
    				@Override
    				public float distance() {
    					final float dist = distance - nearest * nearest;
    					final float offset = MathsUtility.sqrt(radiusSquared - dist);
    					return offset + nearest;
    				}

    				@Override
    				public Normal normal() {
    					// TODO - duplicate code -> Intersection.of()
    					final Vector vec = Vector.between(sphere.centre(), this.point());
    					return new Normal(vec);
    				}
    			};
    		}
    	};

    	// Evaluate intersections
    	return builder.intersections();
	}

//		// Determine length of the nearest point on the ray to the centre of the sphere
//		final Point centre = sphere.centre();
//		final Vector vec = Vector.between(ray.origin(), centre);
//		final float nearest = ray.direction().dot(vec);
//		final float len = vec.magnitude();
//
//		// Check case for sphere behind the ray origin
//		if(nearest < 0) {
//			return intersectBehind(ray, len, nearest);
//		}
//
//		// Calc distance of the nearest point from the sphere centre (using triangles)
//		final float dist = nearest * nearest - len;
//
//		// Stop if ray does not intersect
//		final float radius = sphere.radius();
//		final float radiusSquared = radius * radius;
//		if(Math.abs(dist) > radiusSquared) {
//			return Intersection.NONE;
//		}
//
//		// Create lazy intersection record
//		return () -> {
//			// Calculate offset from nearest point to intersection(s)
//			// TODO - leave as squared?
//			final float offset = MathsUtility.sqrt(radiusSquared - dist);
//
//			// Build intersection results
//			final Intersection a = ray.intersection(nearest + offset, centre);
//			if(len < radiusSquared) {
//				// Ray origin is inside the sphere
//				return List.of(a).iterator();
//			}
//			else {
//				// Ray is outside the sphere (two intersections)
//				final Intersection b = ray.intersection(nearest - offset, centre);
//				return List.of(b, a).iterator();
//			}
//		};
//
//	/**
//	 * Determines intersections for the case where the sphere centre is <i>behind</i> the ray.
//	 * @param ray				Intersection ray
//	 * @param distance			Distance from the ray origin to the centre of the sphere
//	 * @param nearest			Length of the projected nearest point on the ray to the sphere centre
//	 */
//	private Iterable<Intersection> intersectBehind(Ray ray, float distance, float nearest) {
//		final float r = sphere.radius();
//		final float radius = r * r;
//		if(distance > radius) {
//			// Ray originates outside the sphere
//			return Intersection.NONE;
//		}
//		else
//		if(distance < radius) {
//			// Ray originates inside the sphere
////			final var intersection = ray.new AbstractIntersection() {
////				@Override
////				public float distance() {
////					final float dist = distance - nearest * nearest;
////					final float offset = MathsUtility.sqrt(radius - dist);
////					return offset + nearest;
////				}
////
////				@Override
////				public Normal normal() {
////					// TODO - duplicate code -> Intersection.of()
////					final Vector vec = Vector.between(sphere.centre(), this.point());
////					return new Normal(vec);
////				}
////			};
//			return List.of(inside(ray));
//		}
//		else {
//			// Ray originates on the sphere surface
//			return List.of(ray.intersection(0, sphere.centre()));
//		}
//	}
//
//	private static Intersection inside(Ray ray, float distance, float nearest) {
//		return ray.new AbstractIntersection() {
//			@Override
//			public float distance() {
//				final float dist = distance - nearest * nearest;
//				final float offset = MathsUtility.sqrt(radius - dist);
//				return offset + nearest;
//			}
//
//			@Override
//			public Normal normal() {
//				// TODO - duplicate code -> Intersection.of()
//				final Vector vec = Vector.between(sphere.centre(), this.point());
//				return new Normal(vec);
//			}
//		};
//	}

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
}
