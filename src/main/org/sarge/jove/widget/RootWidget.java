package org.sarge.jove.widget;

import org.sarge.jove.common.Location;
import org.sarge.util.ToString;

/**
 * Root (or window) widget.
 * @author Sarge
 */
public class RootWidget extends ContainerWidget implements Renderable {
	private Dimensions dim;

	/**
	 * Constructor.
	 * @param loc		Window screen location
	 * @param dim		Window maximum dimensions
	 * @param layout	Layout
	 */
	public RootWidget( Location loc, Dimensions dim, WidgetLayout layout ) {
		super( layout );
		setLocation( loc );
		setDimensions( dim );
	}
	
	@Override
	public void setLocation( Location loc ) {
		super.setLocation( loc );
	}
	
	/**
	 * Sets the dimensions of this window.
	 * @param w
	 * @param h
	 */
	public void setDimensions( Dimensions dim ) {
		Check.notNull( dim );
		this.dim = dim;
	}
	
	@Override
	public Dimensions getDimensions() {
		return dim;
	}
	
	@Override
	public void render() {
		render( null );
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
