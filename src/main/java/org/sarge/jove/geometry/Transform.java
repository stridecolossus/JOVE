package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * A <i>transform</i> represents a view transformation matrix.
 * @author Sarge
 */
public interface Transform {
	/**
	 * @return Transform matrix
	 */
	Matrix matrix();

	/**
	 * Creates an adapter for the given transform using a custom cosine function.
	 * @param transform		Transform to adapt
	 * @param function		Cosine function
	 * @return Transformer using the custom cosine function
	 */
	static Transform of(Function<CosineFunction, Matrix> transformer, CosineFunction function) {
		requireNonNull(transformer);
		requireNonNull(function);
		return transformer.apply(function);
	}
	// TODO - hmmm
}
