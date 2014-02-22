package org.sarge.jove.animation;

/**
 * Interpolates an animation position.
 * TODO - do we really need this? seems a bit OTT
 * @author Sarge
 */
public interface AnimationInterpolator {
	/**
	 * Interpolator that leaves the position unchanged.
	 */
	AnimationInterpolator NULL = new AnimationInterpolator() {
		@Override
		public float interpolate( float pos ) {
			return pos;
		}
	};

	/**
	 * Interpolates an animation position.
	 * @param pos Position
	 * @return Interpolated position
	 */
	float interpolate( float pos );
}
