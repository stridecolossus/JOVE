package org.sarge.jove.scene;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.MatrixBuilder;
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
		setFieldOfView(MathsUtil.PI / 2f);
	}

	/**
	 * Sets the field-of-view.
	 * @param fov Field-of-view (radians)
	 */
	public void setFieldOfView(float fov) {
		this.h = MathsUtil.tan(fov / 2f);
	}

	@Override
	public float getHeight(Dimensions dim) {
		return h;
	}

	@Override
	public Matrix getMatrix(float near, float far, Dimensions dim) {
		// Calc aspect ratio
		final float f = 1f / h;
		final float ratio = dim.getWidth() / (float) dim.getHeight();

		// Build projection matrix
		return new MatrixBuilder(4)
			.set(0, 0, f / ratio)
			.set(1, 1, f)
			.set(2, 2, (far + near) / (near - far))
			.set(3, 2, -1)
			.set(2, 3, (2f * far * near) / (near - far))
			.build();
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
