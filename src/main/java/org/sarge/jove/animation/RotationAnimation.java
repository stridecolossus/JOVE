package org.sarge.jove.animation;

import org.sarge.jove.geometry.Rotation;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Animated rotation.
 * @author Sarge
 */
public class RotationAnimation implements Animation {
	private final Rotation rot;
	private final float start, end;

	/**
	 * Default constructor that animates a circle.
	 * @param rot Rotation to animate
	 */
	public RotationAnimation( Rotation rot ) {
		this( rot, 0, MathsUtil.TWO_PI );
	}

	/**
	 * Constructor.
	 * @param rot		Rotation to animate
	 * @param start		Start angle (radians)
	 * @param end		End angle
	 */
	public RotationAnimation( Rotation rot, float start, float end ) {
		Check.notNull( rot );
		this.rot = rot;
		this.start = start;
		this.end = end;
	}

	@Override
	public float getMinimum() {
		return start;
	}

	@Override
	public float getMaximum() {
		return end;
	}

	@Override
	public void update( float pos ) {
		rot.setAngle( pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
