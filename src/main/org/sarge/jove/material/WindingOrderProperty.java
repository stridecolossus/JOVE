package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.util.ToString;

/**
 * Triangle winding order property.
 * @author Sarge
 */
public class WindingOrderProperty implements RenderProperty {
	private final boolean order;

	/**
	 * Constructor.
	 * @param order Winding order, default is <tt>true</tt> for the default counter-clockwise order
	 */
	public WindingOrderProperty( boolean order ) {
		this.order = order;
	}

	@Override
	public String getType() {
		return "winding-order";
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setWindingOrder( order );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setWindingOrder( !order );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
