package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.lib.util.Check;

/**
 * Literal colour.
 * @author Sarge
 */
public class LiteralColourFactory implements ColourFactory {
	/**
	 * White particle factory.
	 */
	public static final LiteralColourFactory WHITE = new LiteralColourFactory( Colour.WHITE );

	private final Colour col;

	/**
	 * Constructor.
	 * @param col Particle colour
	 */
	public LiteralColourFactory( Colour col ) {
		Check.notNull( col );
		this.col = col;
	}

	@Override
	public Colour getColour() {
		return col;
	}

	@Override
	public String toString() {
		return col.toString();
	}
}
