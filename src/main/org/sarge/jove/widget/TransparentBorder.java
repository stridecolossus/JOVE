package org.sarge.jove.widget;

import org.sarge.util.ToString;

/**
 * Transparent widget border.
 * @author Sarge
 */
public class TransparentBorder implements Border {
	private final Dimensions dim;

	/**
	 * Constructor.
	 * @param dim Border dimensions 
	 */
	public TransparentBorder( Dimensions dim ) {
		Check.notNull( dim );
		this.dim = dim;
	}

	@Override
	public Dimensions getDimensions() {
		return dim;
	}
	
	@Override
	public void render() {
		// Does nowt
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
