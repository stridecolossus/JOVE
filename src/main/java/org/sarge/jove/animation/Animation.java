package org.sarge.jove.animation;

/**
 * Defines something that can be animated.
 * @author Sarge
 */
public interface Animation {
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
	 * @param value Animation position
	 */
	void update( float pos );
}
