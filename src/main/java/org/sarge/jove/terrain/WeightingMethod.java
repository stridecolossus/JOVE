package org.sarge.jove.terrain;


/**
 * Defines a method for generating multi-texture weighting values for terrain.
 * @author Sarge
 * @see WeightFactory
 * @see TerrainBuilder
 */
public interface WeightingMethod {
	/**
	 * Weight based simply on vertex height (Y coordinate).
	 */
	WeightingMethod HEIGHT = new WeightingMethod() {
		@Override
		public float getWeight( float height, float slope ) {
			return height;
		}
	};

	/**
	 * Weight using vertex slope.
	 */
	WeightingMethod SLOPE = new WeightingMethod() {
		@Override
		public float getWeight( float height, float slope ) {
			return slope;
		}
	};


	/**
	 * Derives a multi-texturing weight from the given vertex.
	 * @param vertex Vertex
	 * @return Weight
	 */
	float getWeight( float height, float slope );
}
