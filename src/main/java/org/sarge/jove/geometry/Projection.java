package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtility;

/**
 * A <i>projection</i> generates the projection matrix for a given viewport.
 * @author Sarge
 */
@FunctionalInterface
public interface Projection {
	/**
	 * - dimensions ~ FOV at far distance & aspect, i.e. inferred and fixed (unless FOV or aspect changes)
	 * - dimensions unused? (unless by frustum?)
	 * - both frustum & projection use tan() thingy
	 */

	//const float halfVSide = zFar * tanf(fovY * .5f);
	//const float halfHSide = halfVSide * aspect;

	/**
	 * Builds the projection matrix for the given viewport.
	 * @param viewport Camera viewport
	 * @return Projection matrix
	 */
	Matrix matrix(Viewport viewport);

	/**
	 * Perspective projection with a 60 degree FOV.
	 */
	Projection DEFAULT = perspective(MathsUtility.toRadians(60));

	/**
	 * Creates a perspective projection with the given field-of-view.
	 * @param fov Field-of-view (radians)
	 */
	static Projection perspective(float fov) {
		return viewport -> {
			final float aspect = 1 / viewport.dimensions().ratio();
			final float scale = (float) Math.tan(fov / 2);
			final float near = viewport.near();
			final float far = viewport.far();

			return new Matrix.Builder()
					.set(0, 0, aspect / scale)
					.set(1, 1, 1 / scale)
					.set(2, 2, far / (far - near))
					.set(2, 3, -(near * far) / (far - near))
					.set(3, 2, 1)
					.build();
		};
	}

	/**
	 * Orthographic or flat projection.
	 * TODO - update for Vulkan (see cookbook)
	 */
	Projection FLAT = viewport -> {
		// Determine clipping planes
		final var dimensions = viewport.dimensions();
		final float left = 0;
		final float right = dimensions.width();
		final float top = 0;
		final float bottom = dimensions.height();

		// Build projection matrix
		final float near = viewport.near();
		final float far = viewport.far();
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
	};
}
