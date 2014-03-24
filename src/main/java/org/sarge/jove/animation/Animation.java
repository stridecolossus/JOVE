package org.sarge.jove.animation;

/**
 * Defines something that can be animated.
 * @author Sarge
 */
public interface Animation {
	/**
	 * @return Duration of this animation
	 */
	long getDuration();
	
	/**
	 * @return Minimum value
	 */
	float getMinimum();

	/**
	 * @return Maximum value
	 */
	float getMaximum();

	/**
	 * Updates this animation.
	 * @param pos Animation position
	 */
	void update( float pos );
}
