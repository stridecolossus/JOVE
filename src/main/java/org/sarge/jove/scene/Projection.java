package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;

/**
 * View projection.
 * @author Sarge
 */
public interface Projection {
	/**
	 * @param dim Viewport dimensions
	 * @return Frustum half-height
	 */
	float getHeight( Dimensions dim );

	/**
	 * Creates the matrix for this projection.
	 * @param near	Near plane
	 * @param far	Far plane
	 * @param dim	Viewport dimensions
	 * @return View matrix
	 */
	Matrix getMatrix( float near, float far, Dimensions dim );
}
