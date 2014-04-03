package org.sarge.jove.scene;

import org.sarge.lib.util.Check;

/**
 * Container node for a {@link Mesh}.
 * @author Sarge
 */
public class RenderableNode extends AbstractNode {
	private Renderable renderable;

	/**
	 * Constructor.
	 * @param name		Node name
	 * @param queue		Rendering queue
	 * @param r			Renderable object
	 */
	public RenderableNode( String name, RenderQueue queue, Renderable r ) {
		super( name, queue );
		setRenderable( r );
	}

	/**
	 * Sets the mesh to be rendered by this node.
	 * @param mesh Mesh to render
	 */
	public void setRenderable( Renderable r ) {
		Check.notNull( r );
		this.renderable = r;
	}

	@Override
	public void apply( RenderContext ctx ) {
		renderable.render( ctx );
	}

	@Override
	public void reset( RenderContext ctx ) {
		// Does nowt
	}
}
