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
}
