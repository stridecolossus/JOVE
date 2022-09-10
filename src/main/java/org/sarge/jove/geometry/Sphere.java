package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtil.*;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>sphere</i> is defined by a radius about a centre-point.
 * @author Sarge
 */
public record Sphere(Point centre, float radius) {
	/**
	 * Creates a sphere volume enclosing the given bounds.
	 * @param bounds Bounds
	 * @return Sphere volume
	 */
	public static Sphere of(Bounds bounds) {
		return new Sphere(bounds.centre(), bounds.largest() / 2);
	}

	/**
	 * Constructor.
	 * @param centre Sphere centre
	 * @param radius Radius
	 */
	public Sphere {
		Check.notNull(centre);
		Check.zeroOrMore(radius);
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
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Sphere that) &&
				this.centre.equals(that.centre) &&
				MathsUtil.isEqual(this.radius, that.radius);
	}
}
