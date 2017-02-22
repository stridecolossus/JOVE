package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * Generates the initial direction for new particles in a {@link ParticleSystem}.
 * @author Sarge
 */
@FunctionalInterface
public interface DirectionFactory {
	/**
	 * @return Direction of next particle
	 */
	Vector getDirection();

	/**
	 * @param dir Direction
	 * @return Fixed direction factory
	 */
	static DirectionFactory literal(Vector dir) {
		return () -> dir;
	}

	/**
	 * Generates a spherical direction factory.
	 * @param speed Initial velocity
	 * @return Spherical direction factory
	 */
	static DirectionFactory sphere(float speed) {
		return () -> {
			final Vector vec = new Vector(
				MathsUtil.nextFloat(-1, 1),
				MathsUtil.nextFloat(-1, 1),
				MathsUtil.nextFloat(-1, 1)
			);
			return vec.normalize().multiply(speed);
		};
	}

	/**
	 * Generates a direction factory defined by a cone.
	 * <p>
	 * The <i>half spread</i> is essentially the randomised range of the X-Z coordinates around the axis.
	 * e.g. if the spread is <tt>0.1</tt> and the X-coordinate of the axis is <tt>0.5</tt> the resultant range is <tt>0.4</tt> to <tt>0.6</tt>.
	 * <p>
	 * Note that the Y coordinate is fixed, i.e. This factory assumes the axis is roughly orientated <i>up</i>.
	 * TODO - change so this can specify cone in any 2 (or 3?) dimensions, rather than assuming X-Z, or leave as-is and use rotation?
	 * 
	 * @param axis		Cone axis
	 * @param spread	Half-spread range
	 * @return Cone direction factory
	 */
	static DirectionFactory cone(Vector axis, float spread) {
		return () -> new Vector(
			axis.x + MathsUtil.nextFloat(-spread, spread),
			axis.y,
			axis.z + MathsUtil.nextFloat(-spread, spread)
		);
	}
}
