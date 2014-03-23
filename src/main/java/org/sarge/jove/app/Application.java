package org.sarge.jove.app;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.input.InputEventHandler;
import org.sarge.jove.scene.RenderContext;

/**
 * Application call-back handler.
 * @author Sarge
 */
public interface Application extends InputEventHandler {
	/**
	 * @return Application title
	 */
	String getTitle();

	/**
	 * Initialises this application.
	 * @param dim		Display dimensions
	 * @param sys		Rendering system
	 */
	void init( Dimensions size, RenderingSystem sys ) throws Exception;

	/**
	 * @return Whether this application is running
	 */
	boolean isRunning();

	/**
	 * Render a frame.
	 * @param ctx Rendering context
	 */
	void render( RenderContext ctx );

	/**
	 * Updates the application state.
	 * @param ctx Rendering context
	 */
	void update( RenderContext ctx );

	/**
	 * Closes this application and releases resources.
	 */
	void close();
}
