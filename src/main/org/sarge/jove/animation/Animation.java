package org.sarge.jove.animation;

/**
 * Defines something that can be animated.
 * @author Sarge
 */
public interface Animation {
	/**
	 * Updates this animation.
	 * @param time		Animation time
	 * @param pos		Animation position (always zero if not interpolated)
	 */
	void update( long time, float pos );
}
