package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * Influence on a particle.
 * @author Sarge
 */
@FunctionalInterface
public interface Influence {
	/**
	 * Applies this influence to the given particle.
	 * @param p			Particle
	 * @param elapsed	Time since last update (ms)
	 */
	void apply(Particle p, long elapsed);

	/**
	 * @param vec Acceleration vector
	 * @return Vector influence
	 */
	static Influence vector(Vector vec) {
		Check.notNull(vec);
		return (p, elapsed) -> {
			final float scale = elapsed / 1000f; // TODO - why /1000f?
			p.add(vec.multiply(scale));
		};
	}

	/**
	 * @param speed Velocity scalar
	 * @return Velocity influence
	 */
	static Influence velocity(float speed) {
		return (p, elapsed) -> {
			final Vector vec = p.getDirection();
			p.setDirection(vec.multiply(speed)); // TODO - mod by elapsed?
		};
	}

	/**
	 * @param rate Fade rate
	 * @return Alpha fade influence
	 */
	static Influence fade(float rate) {
		Check.isPercentile(rate);
		return (p, elapsed) -> p.setAlpha(rate * p.getAlpha());
	}
}
