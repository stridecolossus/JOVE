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
	// TODO - enforce Matrix4 for this accessor?

	/**
	 * @return Whether this transform has changed (default is {@code false})
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
	Transform BILLBOARD = Matrix4.rotation(Vector.Y, MathsUtil.PI);

	/**
	 * Creates a compound transform.
	 * @param transforms Transforms
	 * @return Compound transform
	 */
	static Transform of(List<Transform> transforms) {
		record Compound(List<Transform> list) implements Transform {
			@Override
			public boolean isDirty() {
				return list.stream().anyMatch(Transform::isDirty);
			}

			@Override
			public Matrix matrix() {
				return list.stream().map(Transform::matrix).reduce(Matrix4.IDENTITY, Matrix::multiply);
			}
		}

		return new Compound(List.copyOf(transforms));
	}
}
