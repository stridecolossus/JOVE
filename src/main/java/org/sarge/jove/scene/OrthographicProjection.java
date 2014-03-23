package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;

/**
 * Orthographic (or flat) projection.
 * @author Sarge
 */
public class OrthographicProjection implements Projection {
	@Override
	public float getHeight( Dimensions dim ) {
		return dim.getHeight();
	}

	@Override
	public Matrix getMatrix( float near, float far, Dimensions dim ) {
		// Determine clipping planes
		final float left = 0;
		final float right = dim.getWidth();
		final float top = 0;
		final float bottom = dim.getHeight();

		// Build projection matrix
		final float[][] matrix = new float[ 4 ][ 4 ];
	    matrix[0][0] = 2f / ( right - left );
	    matrix[1][1] = 2f / ( top - bottom );
	    matrix[3][2] = -2f / ( far - near );
	    matrix[0][3] = -( right + left ) / ( right - left );
	    matrix[1][3] = -( top + bottom ) / ( top - bottom );
	    matrix[2][3] = -( far + near ) / ( far - near );
	    matrix[3][3] = 1;
	    return new Matrix( matrix );
	}
}
