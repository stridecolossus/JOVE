package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.*;

import org.sarge.jove.geometry.Vector;

/**
 *
 * @author Sarge
 */
public class WaveInfluence implements Influence {
	private final float period;
	private final Vector wave;
	// TODO - interpolator

	/**
	 * Constructor.
	 * @param period		Oscillation period
	 * @param wave			Wave (or amplitude) vector
	 */
	public WaveInfluence(long period, Vector wave) {
		this.period = oneOrMore(period);
		this.wave = notNull(wave);
	}

	@Override
	public void apply(Particle p, float elapsed) {
		final float pos = elapsed % period;
		final Vector vec = wave.multiply(pos);
		p.move(vec);
	}
}
