package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtil.cos;
import static org.sarge.jove.util.MathsUtil.sin;

import org.sarge.jove.util.MathsUtil;

/**
 * TODO
 * @author Sarge
 */
public final class Sphere {
	// TODO - will we ever need this class? integrate into sphere volume?
	// http://www.songho.ca/opengl/gl_sphere.html
	private Sphere() {
	}

	/**
	 * Calculates the point on the unit-sphere for the given rotation angles (in radians).
	 * <p>
	 * Note that the coordinate space of the unit-sphere is <b>not</b> aligned by default with the Vulkan coordinate system.
	 * The {@link #pointRotated(float, float)} and {@link #swizzle()} methods can be used to transform the coordinate space.
	 * <p>
	 * @param theta		Horizontal angle (or <i>yaw</i>) in the range zero to {@link MathsUtil#TWO_PI}
	 * @param phi		Vertical angle (or <i>pitch</i>) in the range +/- {@link MathsUtil#HALF_PI}
	 * @return Unit-sphere surface point
	 */
	public static Point point(float theta, float phi) {
		final float cos = cos(phi);
		final float x = cos(theta) * cos;
		final float y = sin(theta) * cos;
		final float z = sin(phi);
		return new Point(x, y, z);
	}

	/**
	 * Calculates a point on the unit-sphere with a 90 degree counter-clockwise rotation on the horizontal angle to rotate the coordinate space to the -Z axis.
	 * @param theta		Horizontal angle (or <i>yaw</i>) in the range zero to {@link MathsUtil#TWO_PI}
	 * @param phi		Vertical angle (or <i>pitch</i>) in the range +/- {@link MathsUtil#HALF_PI}
	 * @return Unit-sphere surface point
	 * @see #point(float, float)
	 */
	public static Point pointRotated(float theta, float phi) {
		return point(theta - MathsUtil.HALF_PI, phi);
	}

	/**
	 * Swizzles a sphere surface point to the Vulkan coordinate space.
	 * Specifically this function transposes the X and Z coordinate of the surface point.
	 * @return Transformed point
	 */
	public static Point swizzle(Point pt) {
		return new Point(pt.x, pt.z, pt.y);
	}
}
