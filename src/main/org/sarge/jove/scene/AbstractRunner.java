package org.sarge.jove.scene;

import org.sarge.jove.app.Application;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.util.ToString;

/**
 * Base-class application runner.
 * @author Sarge
 */
public abstract class AbstractRunner {
	private final RenderContext ctx;

	/**
	 * Constructor.
	 */
	protected AbstractRunner() {
		this.ctx = new RenderContext( getRenderingSystem() );
	}

	/**
	 * @return Rendering system for the current platform
	 */
	protected abstract RenderingSystem getRenderingSystem();

	/**
	 * @return Rendering context
	 */
	protected RenderContext getRenderContext() {
		return ctx;
	}

	/**
	 * Renders a frame.
	 * @param app Application
	 */
	public void render( Application app ) {
		ctx.update();
		app.update( ctx );
		app.render( ctx );
		ctx.getRenderingSystem().update();
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
