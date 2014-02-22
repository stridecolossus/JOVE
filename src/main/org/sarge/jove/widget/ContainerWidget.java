package org.sarge.jove.widget;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Location;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.input.InputEvent;

/**
 * Widget container.
 * @author Sarge
 * TODO - implement parent and alter add(), remove(), contains()
 */
public class ContainerWidget extends Widget {
	private final List<Widget> contents = new ArrayList<Widget>(); // TODO - strict
	
	private WidgetLayout layout;
	private Background back = Background.TRANSPARENT;
	private Dimensions dim = new Dimensions();

	/**
	 * Constructor.
	 * @param layout Container layout
	 */
	public ContainerWidget( WidgetLayout layout ) {
		setLayout( layout );
	}
	
	/**
	 * @return Background colour of this container or <tt>null</tt> if transparent
	 */
	public Background getBackground() {
		return back;
	}
	
	/**
	 * Sets the background colour of this container.
	 * @param col Background colour or <tt>null</tt> if transparent
	 */
	public void setBackground( Background back ) {
		this.back = back;
	}

	/**
	 * @return Layout of this widget
	 */
	public WidgetLayout getLayout() {
		return layout;
	}
	
	/**
	 * Sets the layout of this container.
	 * @param layout Container layout
	 */
	public void setLayout( WidgetLayout layout ) {
		Check.notNull( layout );
		this.layout = layout;
		update();
	}
	
	/**
	 * Updates the dimensions of this container.
	 */
	private void update() {
		dim = layout.apply( super.getLocation(), contents );
	}

	@Override
	public Dimensions getDimensions() {
		return dim;
	}
	
	@Override
	protected List<Widget> getChildren() {
		return contents;
	}
	
	/**
	 * Finds the widget at the given location.
	 * @param loc Screen location
	 * @return Selected widget or <tt>null</tt> if none
	 */
	public Widget find( Location loc ) {
		for( Widget w : contents ) {
			// Check whether intersects widget
			if( !w.isVisible() ) continue;
			if( w.contains( loc ) ) return w;
			
			// Recurse into child containers
			if( w instanceof ContainerWidget ) {
				final ContainerWidget c = (ContainerWidget) w;
				final Widget result = c.find( loc );
				if( result != null ) return result;
			}
		}
		
		// No intersected widget
		return null;
	}

	/**
	 * Adds a widget to this container.
	 * @param w Widget to add
	 * @throws IllegalArgumentException if the widget is already contained
	 */
	public void add( Widget w ) {
		Check.notNull( w );
		if( w == this ) throw new IllegalArgumentException( "Cannot add self to container" );
		if( w.parent != null ) throw new IllegalArgumentException( "Widget already contained: " + w );
		
		contents.add( w );
		w.parent = this;
		update();
	}

	/**
	 * Removes a widget from this container.
	 * @param w Widget to remove
	 */
	public void remove( Widget w ) {
		contents.remove( w );
		w.parent = null;
		update();
	}

	/**
	 * Clears contents of this container.
	 */
	public void clear() {
		for( Widget w : contents ) {
			w.parent = null;
		}
		contents.clear();
		update();
	}

	@Override
	public void render( Object obj ) {
		// Translate to origin of this container
		final Location loc = getLocation();
		/* final Matrix m = */ Matrix.translation( new Point( loc.getX(), loc.getY(), 0 ) );
		// TODO - multiply by parent, pass to children
		
		// Render background
		back.render();
		
		// Render contents
		for( Widget w : contents ) {
			if( !w.isVisible() ) continue;
			w.render( null );
		}
	}
	
	@Override
	protected boolean handle( InputEvent e ) {
		for( Widget w : contents ) {
			if( !w.isVisible() ) continue;
			final boolean handled = w.handle( e );
			if( handled ) return true;
		}
		return false;
	}
}
