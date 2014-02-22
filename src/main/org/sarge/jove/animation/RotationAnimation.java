package org.sarge.jove.animation;

import org.sarge.jove.geometry.Rotation;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Animated rotation.
 * @author Sarge
 */
public class RotationAnimation implements Animation {
	private final Rotation rot;

	/**
	 * Constructor.
	 * @param node Node to rotate
	 * @param axis Rotation axis
	 */
	public RotationAnimation( Rotation rot ) {
		Check.notNull( rot );
		this.rot = rot;
	}

	@Override
	public void update( long time, float pos ) {
		rot.setAngle( pos );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
