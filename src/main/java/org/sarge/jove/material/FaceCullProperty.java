package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.FaceCulling;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Front/back face culling.
 * @author Sarge
 */
public class FaceCullProperty implements RenderProperty {
	private final FaceCulling face;

	/**
	 * Constructor.
	 * @param face Faces to cull
	 */
	public FaceCullProperty( FaceCulling face ) {
		Check.notNull( face );
		this.face = face;
	}

	@Override
	public String getType() {
		return "face-culling";
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setFaceCulling( face );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setFaceCulling( FaceCulling.BACK );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
