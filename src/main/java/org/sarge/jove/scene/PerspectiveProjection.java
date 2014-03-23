package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.ToString;

/**
 * Perspective projection.
 * @author Sarge
 */
public class PerspectiveProjection implements Projection {
	private float h;

	/**
	 * Constructor.
	 * Default FOV is 90 degrees.
	 */
	public PerspectiveProjection() {
		setFieldOfView( MathsUtil.PI / 2f );
	}

	/**
	 * Sets the field-of-view.
	 * @param fov Field-of-view (radians)
	 */
	public void setFieldOfView( float fov ) {
		this.h = MathsUtil.tan( fov / 2f );
	}

	@Override
	public float getHeight( Dimensions dim ) {
		return h;
	}

	@Override
	public Matrix getMatrix( float near, float far, Dimensions dim ) {
		// Calc aspect ratio
		final float f = 1f / h;
		final float ratio = dim.getWidth() / (float) dim.getHeight();

		// Build projection matrix
		final float[][] matrix = new float[ 4 ][ 4 ];
		matrix[0][0] = f / ratio;
		matrix[1][1] = f;
		matrix[2][2] = ( far + near ) / ( near - far );
		matrix[3][2] = -1;
		matrix[2][3] = ( 2f * far * near ) / ( near - far );

		return new Matrix( matrix );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
