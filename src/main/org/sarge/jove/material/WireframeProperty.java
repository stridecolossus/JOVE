package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.util.ToString;

/**
 * Wire-frame render property.
 * @author Sarge
 */
public class WireframeProperty implements RenderProperty {
	@Override
	public String getType() {
		return "wireframe";
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setWireframeMode( true );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setWireframeMode( false );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
