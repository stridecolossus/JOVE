package org.sarge.jove.widget;

import java.util.Collections;
import java.util.List;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;
import org.sarge.jove.input.InputEvent;
import org.sarge.util.ToString;

/**
 * Base-class for all widgets.
 * @author Sarge
 */
public abstract class Widget {
	private final MutableLocation loc = new MutableLocation();
	
	private WidgetAlignment horizontal = WidgetAlignment.CENTRE;
	private WidgetAlignment vertical = WidgetAlignment.CENTRE;
	private String tooltip;
	private boolean visible = true;
	
	protected Widget parent;

	/**
	 * @return Screen location of this widget
	 */
	public Location getLocation() {
		return loc;
	}
	
	/**
	 * Sets the location of this widget.
	 * @param loc Widget screen location
	 */
	protected void setLocation( Location loc ) {
		this.loc.set( loc.getX(), loc.getY() );
	}
	
	/**
	 * @param loc Screen location
	 * @return Whether this widget is intersected by the given location
	 */
	protected boolean contains( Location loc ) {
		// Check versus top-left
		if( loc.getX() < this.loc.getX() ) return false;
		if( loc.getY() < this.loc.getY() ) return false;

		// Check versus bottom-right
		final Dimensions dim = getDimensions();
		if( loc.getX() > this.loc.getX() + dim.getWidth() ) return false;
		if( loc.getY() > this.loc.getY() + dim.getHeight() ) return false;
		
		// Location is intersected
		return true;
	}
	
	/**
	 * @return Dimensions of this widget
	 */
	public abstract Dimensions getDimensions();

	/**
	 * @return Horizontal alignment
	 */
	public WidgetAlignment getHorizontalAlignment() {
		return horizontal;
	}
	
	/**
	 * Sets the horizontal alignment of this widget.
	 * @param horizontal Alignment
	 */
	public void setHorizontalAlignment( WidgetAlignment horizontal ) {
		this.horizontal = horizontal;
	}
	
	/**
	 * @return Vertical alignment
	 */
	public WidgetAlignment getVerticalAlignment() {
		return vertical;
	}
	
	/**
	 * Sets the vertical alignment of this widget.
	 * @param vertical Alignment
	 */
	public void setVerticalAlignment( WidgetAlignment vertical ) {
		this.vertical = vertical;
	}
	
	/**
	 * @return Whether this widget is visible.
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * Sets whether this widget is visible.
	 * @param visible Whether this widget is to be rendered
	 */
	public void setVisible( boolean visible ) {
		this.visible = visible;
	}
	
	/**
	 * @return Tooltip string or <tt>null</tt> if none
	 */
	public String getTooltip() {
		return tooltip;
	}
	
	/**
	 * Sets the tooltip string.
	 * @param tooltip Tooltip or <tt>null</tt> if none
	 */
	public void setTooltip( String tooltip ) {
		this.tooltip = tooltip;
	}
	
	/**
	 * @return Parent widget or <tt>null</tt> if not contained
	 */
	public Widget getParent() {
		return parent;
	}

	/**
	 * @return Children of this widget
	 */
	protected List<Widget> getChildren() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Handles the given event.
	 * @param e Input event descriptor
	 * @return Whether this widget successfully handled the event
	 */
	protected boolean handle( InputEvent e ) {
		return false;
	}
	
	/**
	 * Renders this widget.
	 */
	protected abstract void render( Object obj );
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
