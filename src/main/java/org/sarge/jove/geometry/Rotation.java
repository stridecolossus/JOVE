package org.sarge.jove.geometry;

/**
 * A <i>rotation</i> defines a <b>counter-clockwise</b> rotation about an axis implemented as a matrix.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return This rotation as an axis-angle
	 */
	AxisAngle toAxisAngle();

	/**
	 * Rotates the given vector by this rotation.
	 * @param vec Vector to rotate
	 * @return Rotated vector
	 */
	Vector rotate(Vector vec);
}
