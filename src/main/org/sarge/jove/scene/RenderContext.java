package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.light.Light;
import org.sarge.jove.material.Material;
import org.sarge.jove.material.RenderProperty;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Rendering context.
 * @author Sarge
 */
public class RenderContext implements Node.Visitor {
	// Config
	private final RenderingSystem sys;

	// Frame stats
	private long time = System.currentTimeMillis();
	private long elapsed;
	private long total;
	private float fps;
	private int count;

	// Render state
	private final LinkedList<Node> stack = new LinkedList<>();
	private final List<Light> lights = new ArrayList<>();
	private final Map<String, RenderProperty> removed = new HashMap<>();
	private final Map<String, RenderProperty> prev = new HashMap<>();
	private Scene current;

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
	 * @return Scene currently being rendered
	 */
	public Scene getScene() {
		return current;
	}

	/**
	 * Sets the current scene.
	 * @param scene
	 */
	protected void setScene( Scene scene ) {
		this.current = scene;
	}

	/**
	 * @return Current node stack
	 */
	protected LinkedList<Node> getStack() {
		return stack;
	}

	/**
	 * @return Active lights
	 */
	public List<Light> getLights() {
		return lights;
	}

	/**
	 * @return Local model matrix of the current node
	 */
	public Matrix getModelMatrix() {
		return stack.peek().getWorldTransform().toMatrix();
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
	}

	/**
	 *
	 * @param node
	 * @param map
	 */
	private static void add( Node node, Map<String, RenderProperty> map ) {
		final Material mat = node.getMaterial();
		if( mat == null ) return;
		map.putAll( mat.getRenderProperties() );
	}

	@Override
	public boolean visit( Node node ) {
		// Init
		removed.clear();
		prev.clear();

		// Remove previous node(s) from stack and build set of applied properties
		final Node parent = node.getParent();
		if( parent != null ) {
			while( true ) {
				// Stop at parent and record properties to be restored
				final Node n = stack.peek();
				if( n == parent ) {
					add( n, prev );
					break;
				}

				// Remove siblings and record properties to be removed
				stack.pop();
				add( n, removed );
			}
		}

		// Record node
		stack.push( node );

		// Remove properties applied from siblings
		for( RenderProperty p : removed.values() ) {
			p.reset( sys );
		}

		// Apply material
		final Material mat = node.getMaterial();
		if( mat != null ) {
			mat.apply( this );
			for( String name : mat.getRenderProperties().keySet() ) {
				prev.remove( name );
			}
		}

		// Restore parent properties
		for( RenderProperty p : prev.values() ) {
			p.apply( sys );
		}

		// Render node
		final Renderable r = node.getRenderable();
		if( r != null ) {
			r.render( this );
		}

		return true;
	}

	/**
	 * Resets all render properties.
	 */
	protected void reset() {
		while( !stack.isEmpty() ) {
			final Node node = stack.pop();
			final Material mat = node.getMaterial();
			if( mat == null ) continue;
			for( RenderProperty p : mat.getRenderProperties().values() ) {
				p.reset( sys );
			}
		}
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
