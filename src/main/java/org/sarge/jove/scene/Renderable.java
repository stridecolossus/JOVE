package org.sarge.jove.scene;

/**
 * Defines an object that can be rendered.
 * @author Sarge
 */
public interface Renderable {
	/**
	 * Renders this object.
	 * @param ctx Rendering context
	 */
	void render( RenderContext ctx );
}
