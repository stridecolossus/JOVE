package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Blending render property.
 * @author Sarge
 */
public class BlendProperty implements RenderProperty {
	private final String src, dest;

	public BlendProperty( String src, String dest ) {
		Check.notEmpty( src );
		Check.notEmpty( dest );

		this.src = src;
		this.dest = dest;
	}

	public String getSourceFunction() {
		return src;
	}

	public String getDestinationFunction() {
		return dest;
	}

	@Override
	public String getType() {
		return "blend";
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setBlend( this );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setBlend( null );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
