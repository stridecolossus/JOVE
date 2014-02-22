package org.sarge.jove.widget;

import java.util.Collections;
import java.util.List;

import org.sarge.jove.input.InputEvent;

/**
 * Adapter for a {@link Widget} within an enclosing {@link Border}.
 * @author Sarge
 */
public class BorderWidget extends Widget {
	private Border border;
	private Widget widget;
	private Dimensions dim;
	
	/**
	 * Constructor.
	 * @param border Border
	 * @param widget Delegate widget
	 */
	public BorderWidget( Border border, Widget widget ) {
		Check.notNull( border );
		Check.notNull( widget );

		this.border = border;
		this.widget = widget;
		update();
	}
	
	/**
	 * Sets the border.
	 * @param border Border
	 */
	public void setBorder( Border border ) {
		Check.notNull( border );
		this.border = border;
		update();
	}
	
	/**
	 * Sets the underlying widget.
	 * @param widget Delegate widget
	 */
	public void setWidget( Widget widget ) {
		Check.notNull( widget );
		if( widget == this ) throw new IllegalArgumentException( "Cannot enclose self" );
		this.widget = widget;
		update();
	}
	
	/**
	 * Updates dimensions of this widget.
	 */
	private void update() {
		final Dimensions inc = new Dimensions( 2 * border.getWidthInset(), 2 * border.getHeightInset() );
		this.dim = widget.getDimensions().expand( inc );
	}
	
	@Override
	public Dimensions getDimensions() {
		return dim;
	}
	
	@Override
	protected List<Widget> getChildren() {
		return Collections.singletonList( widget );
	}
	
	@Override
	protected boolean handle( InputEvent e ) {
		return widget.handle( e );
	}
	
	@Override
	protected void render( Object obj ) {
		border.render();
		widget.render( null );
	}
}
