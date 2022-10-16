package org.sarge.jove.geometry;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return This rotation as an axis-angle
	 */
	AxisAngle toAxisAngle();

	/**
	 * Rotates the given vector by this rotation.
	 * @param vec Vector
	 * @return Rotated vector
	 */
	Vector rotate(Vector vec);
}
