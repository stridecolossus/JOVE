package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.positive;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
import org.sarge.jove.geometry.Ray.Intersection;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>sphere volume</i> is defined by a simple sphere.
 * @author Sarge
 */
public class SphereVolume implements Volume {
	/**
	 * Helper - Creates a sphere volume that contains the given extents.
	 * @param extents Extents
	 * @return Sphere volume
	 */
	public static SphereVolume of(Extents extents) {
		final float radius = extents.largest() * MathsUtil.HALF;
		return new SphereVolume(extents.centre(), radius);
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
	public Extents extents() {
		final Vector vec = new Vector(radius, radius, radius);
		final Point min = centre.add(vec.invert());
		final Point max = centre.add(vec);
		return new Extents(min, max);
	}

	@Override
	public boolean contains(Point pt) {
		return within(centre.distance(pt), radius);
	}

	@Override
	public boolean intersects(Volume vol) {
		if(vol instanceof SphereVolume sphere) {
			// Intersects if the distance between the centres is within their combined radius
			final float dist = centre.distance(sphere.centre);
			return within(dist, radius + sphere.radius);
		}
		else {
			return intersects(vol.extents());
		}
	}

	/**
	 * Tests whether this sphere is intersected by the given extents.
	 * @param extents Extents
	 * @return Whether intersected
	 */
	public boolean intersects(Extents extents) {
		final float dist = extents.nearest(centre).distance(centre);
		return within(dist, radius);
	}

	/**
	 * @param dist Distance squared
	 * @return Whether the given squared distance is less-than-or-equal to the given sphere radius
	 */
	protected static boolean within(float dist, float radius) {
		return dist <= radius * radius;
	}

	@Override
	public Stream<Intersection> intersect(Ray ray) {
		final Vector vec = Vector.between(ray.origin(), centre);
		final float angle = vec.dot(ray.direction());
		if(angle < 0) {
			return behind(ray, vec);
		}
		else {
			return ahead(ray, angle);
		}
	}

	/**
	 * Determines intersections for the case where the sphere centre is behind the ray.
	 */
	private Stream<Intersection> behind(Ray ray, Vector vec) {
		final float r = radius * radius;
		final float dist = r - vec.magnitude();
		if(dist > 0) {
			// Ray is outside the sphere
			return Intersection.NONE;
		}
		else
		if(dist < 0) {
			// Ray originates inside the sphere
			// TODO
			return null;
		}
		else {
			// Ray originates on the sphere surface
			return Intersection.stream(ray.origin(), dist); // TODO - root
		}
	}

	/**
	 * Determines intersections for the case where the sphere centre is ahead of the ray.
	 */
	private Stream<Intersection> ahead(Ray ray, float angle) {
		// Determine nearest point by projecting the centre of the sphere onto the ray
		final Vector proj = ray.direction().scale(angle);
		final Point pt = ray.origin().add(proj);

		// Calc distance of the projected point from the centre
		final float d = centre.distance(pt);

		// Determine intersection result
		final float r = radius * radius;
		if(d > r) {
			// Ray is outside the sphere
			return Intersection.NONE;
		}
		else
		if(d < r) {
			// Ray intersects the sphere (twice)
			final float base = MathsUtil.sqrt(proj.magnitude());
			final float offset = MathsUtil.sqrt(r - d * d);
			final Point i = ray.origin().add(ray.direction().scale(base - offset));
			return Intersection.stream(i, base - offset);

			// TODO - case for inside sphere (one result)

//				final Vector v = ray.direction().scale(offset);
//				final Intersection left = Intersection.of(pt.add(v.invert()), base - offset);
//				final Intersection right = Intersection.of(pt.add(v), base + offset);
//				return List.of(left, right);
		}
		else {
			// Ray is a tangent
			return Intersection.stream(pt, d);
		}
	}

	// https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection#bounding_spheres
	// http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
////	@Override
//	public Stream<Intersection> intersect2222(Ray ray) {
//		// Build vector from sphere to ray origin
//		final Vector vec = Vector.between(centre, ray.origin());
//
//		if(vec.dot(ray.direction()) < 0) {
//			// Sphere is behind the ray origin
//			final float dist = radius * radius - vec.magnitude();
//			if(dist > 0) {
//				// No intersection
//				return Intersection.NONE;
//			}
//			else
//			if(dist < 0) {
//				// Ray origin is within the sphere
//				final Intersection intersection = new Intersection() {
//					@Override
//					public Point point() {
//						final Point pc = ray.direction().project(vec).toPoint();
//						final float d = MathsUtil.sqrt(dist - Vector.between(pc, ray.origin()).magnitude());
//						return ray.origin().add(ray.direction().scale(d));
//					}
//
//					@Override
//					public float distance() {
//						return dist;
//					}
//				};
//				return List.of(intersection);
//			}
//			else {
//				// Ray origin touching sphere
//				return Intersection.of(ray.origin(), dist);
//			}
//		}
//		else {
//			// Determine intersection point
//			final Point pos = ray.direction().project(vec).toPoint();
//			final float dist = centre.distance(pos);
//			final float r = radius * radius;
//			if(dist > r) {
//				// Ray does not intersect
//				return Intersection.NONE;
//			}
//			else {
//				// TODO - this seems very fishy, why length vs radius squared?
//				final float len = vec.magnitude();
//				if(len > r) {
//					// Origin is outside of the sphere
//					return Intersection.NONE;
//				}
//				else
//				if(len < r) {
//					// Origin is inside the sphere
//					// TODO - ???
//					return null; // Optional.empty();
//				}
//				else {
//					// Ray touches sphere
//					return Intersection.of(pos, dist);
//				}
//			}
//		}
//	}

	@Override
	public int hashCode() {
		return Objects.hash(centre, radius);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof SphereVolume that) &&
				this.centre.equals(that.centre) &&
				MathsUtil.isEqual(this.radius, that.radius);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(centre).append("r", radius).build();
	}
}
