package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;

/**
 * Colour fade influence.
 * @author Sarge
 */
public class FadeInfluence implements Influence {
	private final Colour fade;

	/**
	 * Constructor.
	 * @param fade Fade factor
	 */
	public FadeInfluence( Colour fade ) {
		this.fade = fade;
	}

	@Override
	public void apply( Particle p, long elapsed ) {
		p.fade( fade );
	}

	@Override
	public String toString() {
		return String.valueOf( fade );
	}
}
