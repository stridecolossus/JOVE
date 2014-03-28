package org.sarge.jove.scene;

import java.util.LinkedList;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.material.Material;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Rendering context.
 * @author Sarge
 */
public class RenderContext {
	// Config
	private final RenderingSystem sys;

	// Frame stats
	private long time = System.currentTimeMillis();
	private long elapsed;
	private long total;
	private float fps;
	private int count;

	// Render state
	private final LinkedList<Material> stack = new LinkedList<>();
	private Scene current;
	private Matrix model;

	/**
	 * Constructor.
	 * @param sys Rendering system
	 */
	public RenderContext( RenderingSystem sys ) {
		Check.notNull( sys );
		this.sys = sys;
	}

	/**
	 * @return Rendering system
	 */
	public RenderingSystem getRenderingSystem() {
		return sys;
	}

	/**
	 * @return Current system time (ms)
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return Time since last frame (ms)
	 */
	public long getElapsed() {
		return elapsed;
	}

	/**
	 * @return Current frames-per-second
	 */
	public float getFramesPerSecond() {
		return fps;
	}

	/**
	 * Updates elapsed time and frame-rate stats.
	 */
	void update() {
		// Update times
		final long now = System.currentTimeMillis();
		elapsed = now - time;
		time = now;

		// Update frames-per-second
		++count;
		total += elapsed;
		if( total > 1000 ) {
			fps = count * 1000f / total;
			count = 0;
			total = total % 1000;
		}

		// Reset
		stack.clear();
	}

	/**
	 * @return Scene currently being rendered
	 */
	public Scene getScene() {
		return current;
	}

	/**
	 * Sets the current scene.
	 * @param scene
	 */
	void setScene( Scene scene ) {
		this.current = scene;
	}

	/**
	 * @return Current model matrix
	 */
	public Matrix getModelMatrix() {
		return model;
	}

	/**
	 * Sets the model matrix for the current node.
	 * @param model Model matrix
	 */
	void setModelMatrix( Matrix model ) {
		this.model = model;
	}

	/**
	 * Pushes a material onto the stack.
	 * @param mat Material
	 */
	void push( Material mat ) {
		Check.notNull( mat );
		stack.add( mat );
	}

	/**
	 * @return Most recent material from the stack or <tt>null</tt> if none
	 */
	Material pop() {
		stack.removeLast();
		return stack.peekLast();
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
