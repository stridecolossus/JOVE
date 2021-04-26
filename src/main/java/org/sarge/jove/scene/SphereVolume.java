package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.positive;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray;
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
		// TODO - messy
		final Vector vec = new Vector(radius, radius, radius);
		return new Extents(centre.add(vec.invert()), centre.add(vec));
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

	// https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection#bounding_spheres
	// http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
	// https://www.scratchapixel.com/lessons/3d-basic-rendering/minimal-ray-tracer-rendering-simple-shapes/ray-sphere-intersection
	@Override
	public Optional<Point> intersect(Ray ray) {

		/**
		 * TODO
		 * - create local class ray, vec
		 * - implements Intersection
		 * - lazily evaluates intersection point
		 *
		 */


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
