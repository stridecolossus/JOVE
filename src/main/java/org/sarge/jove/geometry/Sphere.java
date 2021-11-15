package org.sarge.jove.geometry;

import static org.sarge.jove.util.MathsUtil.cos;
import static org.sarge.jove.util.MathsUtil.sin;

import org.sarge.jove.util.MathsUtil;

/**
 * Sphere helper.
 * @author Sarge
 */
public final class Sphere {
	// TODO - will we ever need this class? integrate into sphere volume?
	private Sphere() {
	}

	/**
	 * A <i>sphere point factory</i> generates a point on the unit-sphere given horizontal and vertical angles.
	 * <p>
	 * Note that the coordinate space of the unit-sphere is <b>not</b> aligned by default with the Vulkan coordinate system.
	 * The {@link #rotate()} and {@link #swizzle()} adapters can be used to map the calculated points.
	 * <p>
	 * Usage:
	 * <pre>
	 * PointFactory factory = PointFactory.DEFAULT.swizzle().rotate();
	 * Point p = factory.point(yaw, pitch);
	 * </pre>
	 */
	public interface PointFactory {
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
		PointFactory DEFAULT = (theta, phi) -> {
			final float cos = cos(phi);
			final float x = cos(theta) * cos;
			final float y = sin(theta) * cos;
			final float z = sin(phi);
			return new Point(x, y, z);
		};

		/**
		 * Creates an adapter that transforms the calculated point to the Vulkan coordinate space.
		 * Specifically this function transposes the Y and Z coordinate of the surface point.
		 * @return Swizzle adapter
		 */
		default PointFactory swizzle() {
			return (theta, phi) -> {
				final Point pt = point(theta, phi);
				return new Point(pt.x, pt.z, pt.y);
			};
		}

		/**
		 * Creates an adapter that aligns the default direction of the unit-sphere with the -Z axis.
		 * Specifically this function applies a 90 degree counter-clockwise rotation to the horizontal angle.
		 * @return Rotation adapter
		 */
		default PointFactory rotate() {
			return (theta, phi) -> point(theta - MathsUtil.HALF_PI, phi);
		}
	}
}
