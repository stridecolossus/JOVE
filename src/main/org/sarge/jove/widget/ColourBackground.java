package org.sarge.jove.widget;

import org.sarge.jove.util.Colour;
import org.sarge.util.ToString;

/**
 * Solid colour background.
 * @author Sarge
 */
public class ColourBackground implements Background {
	private Colour col;
	
	/**
	 * Constructor.
	 * @param col Background colour
	 */
	public ColourBackground( Colour col ) {
		setColour( col );
	}
	
	/**
	 * @return Background colour
	 */
	public Colour getColour() {
		return col;
	}
	
	/**
	 * Sets the background colour.
	 * @param col Background colour
	 */
	public void setColour( Colour col ) {
		Check.notNull( col );
		this.col = col;
	}
	
	@Override
	public void render() {
		// TODO - quad with colour
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
