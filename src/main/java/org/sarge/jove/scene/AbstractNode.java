package org.sarge.jove.scene;

import org.sarge.jove.geometry.Matrix;
import org.sarge.lib.util.Check;

/**
 * Scene-graph node.
 * @author Sarge
 */
public abstract class AbstractNode implements Node {
	private final String name;

	private RenderQueue queue;
	private NodeGroup parent;

	/**
	 * Constructor with a specific render queue.
	 * @param name Node name
	 */
	public AbstractNode( String name, RenderQueue queue ) {
		Check.notEmpty( name );
		this.name = name;
		setRenderQueue( queue );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public NodeGroup getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this node.
	 * @param parent New parent or <tt>null</tt> if none
	 */
	public void setParent( NodeGroup parent ) {
		if( this.parent != null ) {
			this.parent.remove( this );
			this.parent = null;
		}

		if( parent != null ) {
			parent.add( this );
		}

		this.parent = parent;
	}

	/**
	 * @return Root node of this scene-graph
	 */
	public Node getRoot() {
		Node node = this;
		while( node.getParent() != null ) {
			node = node.getParent();
		}
		return node;
	}

	@Override
	public RenderQueue getRenderQueue() {
		return queue;
	}

	/**
	 * Sets the rendering queue for this node.
	 * @param queue Rendering queue for this node
	 */
	public void setRenderQueue( RenderQueue queue ) {
		Check.notNull( queue );
		this.queue = queue;
	}

	@Override
	public Matrix getWorldMatrix() {
		if( parent == null ) {
			return Matrix.IDENTITY;
		}
		else {
			return parent.getWorldMatrix();
		}
	}

	@Override
	public void accept( Visitor visitor ) {
		visitor.visit( this );
	}

	@Override
	public String toString() {
		return name;
	}
}
