package org.sarge.jove.geometry;

/**
 * Transformation.
 * @author Sarge
 */
public interface Transform {
	/**
	 * @return This transform as a matrix
	 */
	Matrix toMatrix();

	/**
	 * @return Whether this transform has been updated
	 */
	boolean isDirty();
}
