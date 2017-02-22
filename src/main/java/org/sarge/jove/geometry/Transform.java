package org.sarge.jove.geometry;

/**
 * Transformation.
 * @author Sarge
 */
@FunctionalInterface
public interface Transform {
	/**
	 * @return This transform as a matrix
	 */
	Matrix toMatrix();

	/**
	 * @return Whether this transform has been updated (default is <tt>false</tt> indicating a static transform)
	 */
	default boolean isDirty() {
		return false;
	}
}
