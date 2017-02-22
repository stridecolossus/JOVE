package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MatrixBuilder;

/**
 * Orthographic (or flat) projection.
 * @author Sarge
 */
public class OrthographicProjection implements Projection {
	@Override
	public float getHeight(Dimensions dim) {
		return dim.getHeight();
	}

	@Override
	public Matrix getMatrix(float near, float far, Dimensions dim) {
		// Determine clipping planes
		final float left = 0;
		final float right = dim.getWidth();
		final float top = 0;
		final float bottom = dim.getHeight();

		// Build projection matrix
		return new MatrixBuilder()
			.set(0, 0, 2f / (right - left))
			.set(1, 1, 2f / (top - bottom))
			.set(3, 2, -2f / (far - near))
			.set(0, 3, -(right + left) / (right - left))
			.set(1, 3, -(top + bottom) / (top - bottom))
			.set(2, 3, -(far + near) / (far - near))
			.set(3, 3, 1)
			.build();
	}
}
