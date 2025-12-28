package org.sarge.jove.geometry;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Dimensions;

/**
 * The <i>viewport</i> specifies the dimensions of the camera view.
 * @author Sarge
 */
public record Viewport(Dimensions dimensions, float near, float far) {
	/**
	 * Constructor.
	 * @param near				Near clipping plane distance
	 * @param far				Far clipping plane distance
	 * @param dimensions		Viewport dimensions
	 * @throws IllegalArgumentException if {@link #near} is larger than {@link #far}
	 */
	public Viewport {
		if(near > far) {
			throw new IllegalArgumentException();
		}
		requireNonNull(dimensions);
	}

	/**
	 * Constructor with default clipping distances.
	 * @param dimensions Viewport dimensions
	 */
	public Viewport(Dimensions dimensions) {
		this(dimensions, 0.1f, 100);
	}
}
