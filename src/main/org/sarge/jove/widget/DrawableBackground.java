package org.sarge.jove.widget;

import org.sarge.util.ToString;

/**
 * {@link Drawable} background.
 * @author Sarge
 */
public class DrawableBackground implements Background {
	private final Drawable drawable;

	/**
	 * Constructor.
	 * @param drawable Background
	 */
	public DrawableBackground( Drawable drawable ) {
		Check.notNull( drawable );
		this.drawable = drawable;
	}
	
	@Override
	public void render() {
		drawable.render( null );
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
