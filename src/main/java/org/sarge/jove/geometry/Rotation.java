package org.sarge.jove.geometry;

/**
 * A <i>rotation</i> represents as a counter-clockwise transform about an axis.
 * @author Sarge
 */
public interface Rotation extends Transform {
	/**
	 * @return This rotation represented by an axis-angle tuple
	 */
	AxisAngle toAxisAngle();

	/**
	 * Rotates the given vector about this rotation.
	 * @param vector Vector to rotate
	 * @return Rotated vector
	 */
	Vector rotate(Vector vector);
}
