package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.util.MathsUtil;

/**
 * View projection.
 * @author Sarge
 */
public interface Projection {
	/**
	 * Calculates the frustum half-height for this projection.
	 * @param dim Viewport dimensions
	 * @return Frustum half-height
	 */
	float height(Dimensions dim);

	/**
	 * Builds the matrix for this projection.
	 * @param near		Near plane
	 * @param far		Far plane
	 * @param dim		Viewport dimensions
	 * @return Projection matrix
	 */
	Matrix matrix(float near, float far, Dimensions dim);

	/**
	 * Perspective projection with a 90 degree FOV.
	 */
	Projection DEFAULT = perspective(MathsUtil.HALF_PI);

	/**
	 * Creates a perspective projection.
	 * @param fov Field-of-view (radians)
	 */
	static Projection perspective(float fov) {
		return new Projection() {
			private final float height = MathsUtil.tan(fov * MathsUtil.HALF);

			@Override
			public float height(Dimensions dim) {
				return height;
			}

			@Override
			public Matrix matrix(float near, float far, Dimensions dim) {
				final float f = 1 / height;
				final float range = near - far;
				return new Matrix.Builder()
					.set(0, 0, f / dim.ratio())
					.set(1, 1, -f)
					.set(2, 2, far / range)
					.set(2, 3, (near * far) / range)
					.set(3, 2, -1)
					.build();
			}
		};
	}

	// https://dovo329.github.io/DeriveOpenGLPerspectiveProjectionMatrix/
	// https://stackoverflow.com/questions/51318119/what-is-the-role-of-gl-position-w-in-vulkan

	/**
	 * Orthographic or flat projection.
	 * TODO - update for Vulkan (see cookbook)
	 */
	Projection FLAT = new Projection() {
		@Override
		public float height(Dimensions dim) {
			return dim.height();
		}

		@Override
		public Matrix matrix(float near, float far, Dimensions dim) {
			// Determine clipping planes
			final float left = 0;
			final float right = dim.width();
			final float top = 0;
			final float bottom = dim.height();

			// Build projection matrix
			return new Matrix.Builder()
				.identity()
				.set(0, 0, 2f / (right - left))
				.set(1, 1, 2f / (top - bottom))
				.set(3, 2, -2f / (far - near))
				.set(0, 3, -(right + left) / (right - left))
				.set(1, 3, -(top + bottom) / (top - bottom))
				.set(2, 3, -(far + near) / (far - near))
				.set(3, 3, 1)
				.build();
		}
	};
}
