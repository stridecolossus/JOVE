package org.sarge.jove.widget;

import java.util.List;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;
import org.sarge.util.ToString;

/**
 * Simple list layout.
 * @author Sarge
 */
public class ListWidgetLayout implements WidgetLayout {
	private final boolean vertical;
	
	/**
	 * Constructor.
	 * @param vertical Whether layout is vertically or horizontally ordered
	 */
	public ListWidgetLayout( boolean vertical ) {
		this.vertical = vertical;
	}
	
	@Override
	public Dimensions apply( Location origin, List<Widget> widgets ) {
		// Update location of widgets according to this layout
		final MutableLocation loc = new MutableLocation( origin );
		float max = 0;
		for( Widget w : widgets ) {
			// Update widget location
			if( !w.isVisible() ) continue;
			w.setLocation( loc );
			
			// Move to next slot and update largest dimension
			final Dimensions dim = w.getDimensions();
			if( vertical ) {
				loc.add( 0, dim.getHeight() );
				max = Math.max( max, dim.getWidth() );
			}
			else {
				loc.add( w.getDimensions().getWidth(), 0 );
				max = Math.max( max, dim.getHeight() );
			}
		}

		// Calc size of this layout
		if( vertical ) {
			return new Dimensions( max, loc.getY() - origin.getY() );
		}
		else {
			return new Dimensions( loc.getX() - origin.getX(), max );
		}
	}
	
	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
