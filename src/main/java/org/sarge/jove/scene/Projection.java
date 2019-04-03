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
	 * Builds the view matrix for this projection.
	 * @param near		Near plane
	 * @param far		Far plane
	 * @param dim		Viewport dimensions
	 * @return View matrix
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
//				final float f = 1f / height;
				return new Matrix.Builder()
					.set(0, 0, 1f / (height * dim.ratio()))
					.set(1, 1, 1f / height)
					.set(2, 2, far / (near - far) * 0.5f)
					.set(2, 3, -1 * 0.5f)
					.set(3, 2, (near * far) / (near - far))
					//.set(3, 3, 1)
					.build();
			}
		};
	}

	// https://stackoverflow.com/questions/51318119/what-is-the-role-of-gl-position-w-in-vulkan
//	.set(0, 0, f / dim.ratio())
//	.set(1, 1, -f)
//	.set(2, 2, far / (near - far))
//	.set(2, 3, -1)
//	.set(3, 2, (near * far) / (near - far))

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
