package org.sarge.jove.animation;

import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Animated rotation.
 * @author Sarge
 */
public class RotationAnimation extends AbstractAnimation {
	private final Rotation rot;

	/**
	 * Default constructor that animates a {@link Rotation} through 360 degrees.
	 * @param duration	Animation duration (ms)
	 * @param rot		Rotation to animate
	 */
	public RotationAnimation( long duration, Rotation rot ) {
		this( duration, 0, MathsUtil.TWO_PI, rot );
	}

	/**
	 * Constructor.
	 * @param duration	Animation duration (ms)
	 * @param start		Start angle (radians)
	 * @param end		End angle
	 * @param rot		Rotation to animate
	 */
	public RotationAnimation( long duration, float start, float end, Rotation rot ) {
		super( duration, start, end );
		Check.notNull( rot );
		this.rot = rot;
	}

	@Override
	public void update( float pos ) {
		rot.setAngle( pos );
	}
}
