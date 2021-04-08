package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>sphere</i>
 * TODO - integrate into SphereVolume
 * @author Sarge
 */
public record Sphere(float radius) {
	/**
	 * Constructor.
	 * @param radius Sphere radius
	 */
	public Sphere {
		Check.positive(radius);
	}

	/**
	 * Constructor for the unit sphere.
	 */
	public Sphere() {
		this(1);
	}

	/**
	 * Calculates the point on the surface of this sphere for the given angles (radians).
	 * @param phi		Horizontal angle
	 * @param theta		Vertical angle
	 * @return Sphere surface point
	 * @see #point(float, float, float)
	 */
	public Vector point(float phi, float theta) {
		return point(phi, theta, radius);
	}

	/**
	 * Calculates the point on the surface of a sphere for the given angles (radians).
	 * <p>
	 * Notes:
	 * <li><i>phi</i> is the counter-clockwise rotation (or <i>yaw</i>) about the Y axis (looking <b>down</b> onto the sphere) in the range 0...2 pi</li>
	 * <li><i>theta</i> is the vertical rotation (or <i>pitch</i>) in the range -/+ half pi</li>
	 * <li>by default the horizontal angle (<i>phi</i>) is zero in the direction of the positive X axis (add -90 degree to align with the Vulkan negative Z direction)</li>
	 * </ul>
	 * <p>
	 * @param phi			Horizontal (or <i>yaw</i>) angle
	 * @param theta			Vertical (or <i>pitch</i>) angle
	 * @param radius		Sphere radius
	 * @return Sphere surface point
	 * @see MathsUtil#PI
	 */
	public static Vector point(float phi, float theta, float radius) {
		final float cos = MathsUtil.cos(theta);
		final float x = radius * cos * MathsUtil.cos(phi);
		final float y = radius * MathsUtil.sin(theta);
		final float z = radius * cos * MathsUtil.sin(phi);
		return new Vector(x, y, z);
	}
}
