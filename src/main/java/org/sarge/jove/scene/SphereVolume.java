package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.positive;

import java.util.List;
import java.util.Objects;

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
	 * Helper - Creates a sphere volume that encloses the given extents.
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
	 * @return Whether the squared distance is less-than-or-equal to the given sphere radius
	 */
	protected static final boolean within(float dist, float radius) {
		return dist <= radius * radius;
	}

	@Override
	public Intersection intersect(Ray ray) {
		// Determine length of the nearest point on the ray to the centre of the sphere
		final Vector vec = Vector.between(ray.origin(), centre);
		final float nearest = ray.direction().dot(vec);

		// Check case for sphere behind the ray origin
		if(nearest < 0) {
			return intersectBehind(ray, vec, nearest);
		}

		// Calc distance of the nearest point from the sphere centre (using triangles)
		final float len = vec.magnitude();
		final float dist = nearest * nearest - len;

		// Stop if ray does not intersect
		final float r = radius * radius;
		if(Math.abs(dist) > r) {
			return Intersection.NONE;
		}

		// Create lazy intersection record
		return () -> {
			// Calculate offset from nearest point to intersection(s)
			final float offset = MathsUtil.sqrt(r - dist);

			// Build intersection results
			final float b = nearest + offset;
			if(len < r) {
				// Ray origin is inside the sphere
				return List.of(b);
			}
			else {
				// Ray is outside the sphere (two intersections)
				final float a = nearest - offset;
				return List.of(a, b);
			}
		};
	}

	/**
	 * Determines intersection for the case where the sphere centre is <i>behind</i> the ray.
	 * @param ray			Intersection ray
	 * @param vec			Vector from the ray origin to the centre of the sphere
	 * @param nearest		Length of the projected nearest point on the ray to the sphere centre
	 */
	protected Intersection intersectBehind(Ray ray, Vector vec, float nearest) {
		// Stop if ray is outside of the sphere
		final float r = radius * radius;
		final float len = vec.magnitude();
		if(len > r) {
			return Intersection.NONE;
		}

		// Otherwise calc intersection point
		if(len < r) {
			/*
			// Ray originates inside the sphere
//			final Vector proj = ray.direction().scale(angle);
//			final Point nearest = ray.origin().add(proj);
			final Point nearest = ray.scale(angle);
			final float d = centre.distance(nearest);
//			final float offset = MathsUtil.sqrt(r - d) - MathsUtil.sqrt(proj.magnitude()); // surely proj.mag() === angle?
			final float offset = MathsUtil.sqrt(r - d);
			return Intersection.stream(ray.scale(offset), offset - MathsUtil.sqrt(angle));
			*/
			return null;
		}
		else {
			// Ray originates on the sphere surface
			return Intersection.of(0f);
		}
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
		return (obj instanceof SphereVolume that) && this.centre.equals(that.centre) && MathsUtil.isEqual(this.radius, that.radius);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(centre).append("r", radius).build();
	}
}
