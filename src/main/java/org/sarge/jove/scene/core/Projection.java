package org.sarge.jove.scene.core;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>view projection</i> generates the projection matrix.
 * @author Sarge
 */
@FunctionalInterface
public interface Projection {
	/**
	 * Builds the matrix for this projection.
	 * @param near		Near plane
	 * @param far		Far plane
	 * @param dim		Viewport dimensions
	 * @return Projection matrix
	 */
	Matrix matrix(float near, float far, Dimensions dim);

	/**
	 * Perspective projection with a 60 degree FOV.
	 */
	Projection DEFAULT = perspective(MathsUtility.toRadians(60));

	/**
	 * Creates a perspective projection.
	 * @param fov Field-of-view (radians)
	 */
	static Projection perspective(float fov) {
		final float scale = 1 / (float) Math.tan(fov / 2);
		return (near, far, dim) -> {
			return new Matrix.Builder(4)
					.set(0, 0, scale / dim.ratio())
					.set(1, 1, -scale)
					.set(2, 2, far / (near - far))
					.set(2, 3, (near * far) / (near - far))
					.set(3, 2, -1)
					.build();
		};
	}

	/**
	 * Orthographic or flat projection.
	 * TODO - update for Vulkan (see cookbook)
	 */
	Projection FLAT = (near, far, dim) -> {
		// Determine clipping planes
		final float left = 0;
		final float right = dim.width();
		final float top = 0;
		final float bottom = dim.height();

		// Build projection matrix
		return new Matrix.Builder(4)
				.identity()
				.set(0, 0, 2f / (right - left))
				.set(1, 1, 2f / (top - bottom))
				.set(3, 2, -2f / (far - near))
				.set(0, 3, -(right + left) / (right - left))
				.set(1, 3, -(top + bottom) / (top - bottom))
				.set(2, 3, -(far + near) / (far - near))
				.set(3, 3, 1)
				.build();
	};
}
