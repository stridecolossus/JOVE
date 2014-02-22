package org.sarge.jove.light;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Directional light source (2).
 * @author Sarge
 */
public class DirectionalLight extends AbstractLight {
	private Vector dir = Vector.Z_AXIS;

	@Override
	public int getType() {
		return 2;
	}

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
