package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.util.ToString;

/**
 * Depth-test render property.
 * @author Sarge
 */
public class DepthTestProperty implements RenderProperty {
	static final DepthTestProperty DEFAULT = new DepthTestProperty( "<" );

	private final String func;

	/**
	 * Constructor.
	 * @param func Depth-test function name or <tt>null</tt> to disable depth-testing
	 */
	public DepthTestProperty( String func ) {
		this.func = func;
	}

	@Override
	public String getType() {
		return "depth-function";
	}

	public String getFunction() {
		return func;
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setDepthTest( this );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setDepthTest( DEFAULT );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
