package org.sarge.jove.geometry;

import java.util.List;

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
	 * @return Whether this is a mutable transformation
	 */
	default boolean isMutable() {
		return false;
	}

	/**
	 * Creates a compound transform.
	 * @param transforms Transforms
	 * @return Compound transform
	 */
	static Transform of(List<Transform> transforms) {
		return new Transform() {
			private final List<Transform> list = List.copyOf(transforms);

			@Override
			public Matrix matrix() {
				return list.stream().map(Transform::matrix).reduce(Matrix4.IDENTITY, Matrix::multiply);
			}
		};
	}
}
