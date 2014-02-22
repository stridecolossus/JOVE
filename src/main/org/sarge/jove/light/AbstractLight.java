package org.sarge.jove.light;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Ambient light source.
 * @author Sarge
 */
public abstract class AbstractLight implements Light {
	private Colour col = Colour.WHITE;

	/**
	 * @return Light colour
	 */
	@Override
	public Colour getColour() {
		return col;
	}

	/**
	 * Sets the colour of this light.
	 * @param col Light colour
	 */
	public void setColour( Colour col ) {
		Check.notNull( col );
		this.col = col;
	}

	@Override
	public Point getPosition() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector getDirection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
