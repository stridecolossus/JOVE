package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.RandomUtil;

/**
 * Generates random (normalised) directions.
 * @author Sarge
 */
public class SphereDirectionFactory implements DirectionFactory {
	private final float speed;

	/**
	 * Constructor.
	 * @param speed Velocity scalar
	 */
	public SphereDirectionFactory( float speed ) {
		this.speed = speed;
	}

	@Override
	public Vector getDirection() {
		final Vector vec = new Vector(
			RandomUtil.nextFloat( -1, 1 ),
			RandomUtil.nextFloat( -1, 1 ),
			RandomUtil.nextFloat( -1, 1 )
		);

		return vec.normalize().multiply( speed );
	}
}
