package org.sarge.jove.geometry;

import java.util.List;

import org.sarge.jove.util.MathsUtil;

/**
 * A <i>transform</i> generates a {@link Matrix} for a model transformation.
 * @author Sarge
 */
@FunctionalInterface
public interface Transform {
	/**
	 * @return Transformation matrix
	 */
	Matrix matrix();

	/**
	 * @return Whether this transform has changed (default is <tt>false</tt>)
	 */
	default boolean isDirty() {
		return false;
	}

	/**
	 * Billboard transform.
	 *
	 * TODO - billboard
	 * TODO - factor out flip by axis
	 */
	Transform BILLBOARD = Matrix.rotation(Vector.Y_AXIS, MathsUtil.PI);

	/**
	 * Creates a compound transform.
	 * @param transforms Transforms
	 * @return Compound transform
	 */
	static Transform of(List<Transform> transforms) {
		return new Transform() {
			private transient Matrix matrix = update();

			/**
			 * @return Compound matrix
			 */
			private Matrix update() {
				return transforms.stream().map(Transform::matrix).reduce(Matrix.IDENTITY, Matrix::multiply);
			}

			@Override
			public Matrix matrix() {
				if(isDirty()) {
					matrix = update();
				}
				return matrix;
			}

			@Override
			public boolean isDirty() {
				return transforms.stream().anyMatch(Transform::isDirty);
			}
		};
	}
}
