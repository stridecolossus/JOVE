package org.sarge.jove.geometry;

import java.util.Optional;

import org.sarge.jove.util.Check;
import org.sarge.jove.util.MathsUtil;

/**
 * Bounding volume defined by a simple sphere.
 * @author Sarge
 */
public record SphereVolume(Point centre, float radius) implements BoundingVolume {
	/**
	 * Creates a sphere volume from the given extents.
	 * @param extents Extents
	 * @return Sphere volume
	 */
	public static SphereVolume of(Extents extents) {
		final float radius = extents.size() * MathsUtil.HALF;
		return new SphereVolume(extents.centre(), radius);
	}

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public SphereVolume {
		Check.notNull(centre);
		Check.zeroOrMore(radius);
	}

	@Override
	public Extents extents() {
		final Vector min = new Vector(-radius, -radius, -radius);
		final Vector max = new Vector(radius, radius, radius);
		return new Extents(centre.add(min), centre.add(max));
	}

	@Override
	public boolean contains(Point pt) {
		return centre.distance(pt) <= radius * radius;
	}

	// http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
	@Override
	public Optional<Point> intersect(Ray ray) {
		// Build vector from sphere to ray origin
		final Vector vec = Vector.between(centre, ray.origin());

		if(vec.dot(ray.direction()) < 0) {
			// Sphere is behind the ray origin
			final float dist = radius * radius - vec.magnitude();
			if(dist > 0) {
				// No intersection
				return Optional.empty();
			}
			else
			if(dist < 0) {
				// Ray origin is within the sphere
				final Point pc = ray.direction().project(vec).toPoint();
				final float d = MathsUtil.sqrt(dist - Vector.between(pc, ray.origin()).magnitude());
				final Point result = ray.origin().add(ray.direction().scale(d));
				return Optional.of(result);
			}
			else {
				// Ray origin touching sphere
				return Optional.of(ray.origin());
			}
		}
		else {
			// Determine intersection point
			final Point pos = ray.direction().project(vec).toPoint();
			final float dist = centre.distance(pos);
			final float r = radius * radius;
			if(dist > r) {
				// Ray does not intersect
				return Optional.empty();
			}
			else {
				final float len = vec.magnitude();
				if(len > r) {
					// Origin is outside of the sphere
					// TODO
					return Optional.empty();
				}
				else
				if(len < r) {
					// Origin is inside the sphere
					// TODO
					return Optional.empty();
				}
				else {
					// Ray touches sphere
					return Optional.of(pos);
				}
			}
		}
	}

	@Override
	public boolean intersects(BoundingVolume vol) {
		return vol.intersects(centre, radius);
	}

	@Override
	public boolean intersects(Point centre, float radius) {
		final float dist = this.centre.distance(centre);
		return intersects(dist, this.radius) || intersects(dist, radius);
	}

	private static boolean intersects(float dist, float radius) {
		return dist <= radius * radius;
	}

	@Override
	public boolean intersects(Extents extents) {
		// TODO
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}

		return
				(obj instanceof SphereVolume that) &&
				MathsUtil.isEqual(this.radius, that.radius) &&
				this.centre.equals(that.centre);
	}
}
