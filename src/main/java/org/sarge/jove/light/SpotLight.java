package org.sarge.jove.light;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Spot-light (4).
 * @author Sarge
 */
public class SpotLight extends PointLight {
	private Vector dir = Vector.Z_AXIS;

// TODO
//	cutoff
//	exponent
//	attenuation: constant, linear, quadratic

	@Override
	public int getType() {
		return 4;
	}

	/**
	 * @return Light direction
	 */
	@Override
	public Vector getDirection() {
		return dir;
	}

	/**
	 * Sets the direction of this light.
	 * @param dir Light direction
	 */
	public void setDirection( Vector dir ) {
		Check.notNull( dir );
		this.dir = dir;
	}
}
