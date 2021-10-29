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
	 * A <i>sphere point function</i> calculates a point on the surface of the unit-sphere.
	 * <p>
	 * Note that the coordinate space of the unit-sphere is <b>not</b> aligned by default with the Vulkan coordinate system.
	 * The {@link #rotate()} and {@link #swizzle()} can be used to transform the coordinate space if required.
	 */
	@FunctionalInterface
	public interface PointFunction {
		/**
		 * Calculates the point on the unit-sphere for the given rotation angles (in radians).
		 * @param theta		Horizontal angle (or <i>yaw</i>) in the range zero to {@link MathsUtil#TWO_PI}
		 * @param phi		Vertical angle (or <i>pitch</i>) in the range +/- {@link MathsUtil#HALF_PI}
		 * @return Unit-sphere surface point
		 */
		Point point(float theta, float phi);

		/**
		 * Default implementation.
		 */
		PointFunction DEFAULT = (theta, phi) -> {
			final float cos = cos(phi);
			final float x = cos(theta) * cos;
			final float y = sin(theta) * cos;
			final float z = sin(phi);
			return new Point(x, y, z);
		};

		/**
		 * Creates an adapter that applies a 90 degree counter-clockwise rotation to the horizontal angle (theta) <i>before</i> calculating the point.
		 * @return Horizontal rotation adapter
		 */
		default PointFunction rotate() {
			return (theta, phi) -> point(theta - MathsUtil.HALF_PI, phi);
		}

		/**
		 * Creates an adapter that transforms the calculated point to the Vulkan coordinate space.
		 * Specifically this function transposes the X and Z coordinate of the surface point.
		 * @return Coordinate space swizzle adapter
		 */
		default PointFunction swizzle() {
			return (theta, phi) -> {
				final Point pt = point(theta, phi);
				return new Point(pt.x, pt.z, pt.y);
			};
		}
	}
}
