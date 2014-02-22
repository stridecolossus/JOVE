package org.sarge.jove.scene;

/**
 * Defines something that can be rendered.
 * @author Sarge
 */
public interface Renderable {
	/**
	 * Renders this object.
	 * @param ctx Rendering context
	 */
	void render( RenderContext ctx );
}
