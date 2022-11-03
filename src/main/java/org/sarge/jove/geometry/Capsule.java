package org.sarge.jove.geometry;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Ray.Intersection;

/**
 * A <i>capsule</i> is a <i>swept sphere</i> volume specified as a given radius about a line segment.
 * @author Sarge
	https://wickedengine.net/2020/04/26/capsule-collision-detection/
 */
public class Capsule implements Volume {
	private final Point top, bottom;
	private final float radius;
	// TODO - top/bottom should be the AB points?
	// TODO - line segment class?

	/**
	 * Constructor.
	 * @param top			Top point
	 * @param bottom		Bottom point
	 * @param radius		Radius
	 */
	public Capsule(Point top, Point bottom, float radius) {
		this.top = notNull(top);
		this.bottom = notNull(bottom);
		this.radius = radius;
	}

	/**
	 * @return Radius
	 */
	public float radius() {
		return radius;
	}

	@Override
	public Bounds bounds() {
		// TODO - this is almost certainly wrong
		return new Bounds(top, bottom);
	}

	@Override
	public boolean contains(Point p) {
		// TODO

		final Vector segment = Vector.between(bottom, top);
		final Vector normal = segment.normalize();

		final Point a = bottom.add(normal);
		final Point b = top.subtract(normal);
		final Vector ab = Vector.between(a, b);

		final float t = p.subtract(a).dot(ab) / ab.dot(ab);
		final Point n = a.add(ab.multiply(saturate(t)));

		return n.distance(p) < radius * radius;
	}

	private static float saturate(float t) {
		return Math.min(1, Math.max(0, t));
	}

	@Override
	public boolean intersects(Volume vol) {
		if(vol instanceof SphereVolume sphere) {
			return false; // TOOD
		}
		else
		if(vol instanceof BoundingBox box) {
			return false; // TOOD
		}
		else {
			return Volume.super.intersects(this);
		}
	}

	@Override
	public boolean intersects(Plane plane) {
		// TODO
		return false;
	}

	@Override
	public Intersection intersection(Ray ray) {
		// TODO
		return null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("start", top)
				.append("end", bottom)
				.append("r", radius)
				.build();
	}
}
