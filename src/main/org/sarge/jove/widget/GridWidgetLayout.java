package org.sarge.jove.widget;

import java.util.List;

import org.sarge.jove.common.Location;
import org.sarge.jove.common.MutableLocation;
import org.sarge.util.ToString;

/**
 * Grid layout.
 * @author Sarge
 */
public class GridWidgetLayout implements WidgetLayout {
	private int rows, cols;
	private float w, h;
	
	/**
	 * Constructor.
	 */
	public GridWidgetLayout( int rows, int cols, float w, float h ) {
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
