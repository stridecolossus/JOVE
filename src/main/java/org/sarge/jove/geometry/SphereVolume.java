package org.sarge.jove.geometry;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Sphere volume.
 * @author Sarge
 */
public class SphereVolume implements BoundingVolume {
	private final Point centre;
	private final float radius;

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public SphereVolume(Point centre, float radius) {
		Check.notNull(centre);
		Check.zeroOrMore(radius);
		this.centre = centre;
		this.radius = radius;
	}

	/**
	 * Constructor for a sphere at the origin.
	 * @param radius Radius
	 */
	public SphereVolume(float radius) {
		this(Point.ORIGIN, radius);
	}

	@Override
	public Point getCentre() {
		return centre;
	}

	/**
	 * @return Sphere radius
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public boolean contains(Point pos) {
		return pos.distanceSquared(centre) <= radius * radius;
	}

	@Override
	public boolean intersects(Ray ray) {
		// Build vector from sphere to ray origin
		Vector vec = Vector.between(centre, ray.getOrigin());

		// Project sphere onto ray (unless behind ray)
		if(vec.dot(ray.getDirection()) >= 0) {
			final Point pos = centre.project(ray.getDirection());
			vec = Vector.between(centre, pos);
		}

		// Check distance to sphere centre
		return vec.getMagnitudeSquared() <= radius * radius;
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
