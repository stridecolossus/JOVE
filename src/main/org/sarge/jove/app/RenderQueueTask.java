package org.sarge.jove.app;

import org.sarge.jove.scene.RenderContext;

/**
 * OpenGL task to be executed on the rendering thread.
 * @author Sarge
 */
public interface RenderQueueTask {
	/**
	 * Executes this task.
	 * @param ctx Rendering context
	 */
	void execute( RenderContext ctx );
}
