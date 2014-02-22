package org.sarge.jove.animation;

import org.sarge.jove.util.MathsUtil;

/**
 * Interpolates an animation position of a given range.
 * @see MathsUtil#lerp(float, float, float)
 * @author Sarge
 */
public class LinearAnimationInterpolator implements AnimationInterpolator {
	private final float start, end;

	/**
	 * Constructor.
	 * @param start		Starting value
	 * @param end		End value
	 */
	public LinearAnimationInterpolator( float start, float end ) {
		this.start = start;
		this.end = end;
	}

	@Override
	public float interpolate( float pos ) {
		return MathsUtil.lerp( pos, start, end );
	}

	@Override
	public String toString() {
		return start + "-" + end;
	}
}
