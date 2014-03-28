package org.sarge.jove.scene;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.sarge.lib.util.StrictList;

/**
 * Set of scene-graph nodes.
 * @author Sarge
 */
public abstract class NodeGroup extends AbstractNode {
	/**
	 * Node state flags.
	 */
	public static enum Flag {
		/**
		 * Transform has changed.
		 */
		TRANSFORM,

		/**
		 * Bounding volume has changed.
		 */
		BOUNDING_VOLUME,

		/**
		 * Scene-graph has changed (children added or removed).
		 */
		GRAPH,
	}

	private final List<Node> children = new StrictList<>();
	private final transient EnumSet<Flag> flags = EnumSet.noneOf( Flag.class );

	/**
	 * Constructor with a specific render queue.
	 * @param name Node name
	 */
	public NodeGroup( String name, RenderQueue queue ) {
		super( name, queue );
	}

	/**
	 * @param flag Status flag
	 * @return Whether the given flag is set on this node
	 */
	public boolean isFlagged( Flag flag ) {
		return flags.contains( flag );
	}

	/**
	 * Sets the specified flag on this node.
	 * @param flag Status flag to set
	 */
	protected void set( Flag flag ) {
		flags.add( flag );
	}

	/**
	 * Clears the specified flag on this node.
	 * @param flag Status flag to clear
	 */
	protected void clear( Flag flag ) {
		flags.remove( flag );
	}

	/**
	 * Propagates the specified flag up the scene-graph.
	 * @param flag Status flag
	 */
	protected void propagate( Flag flag ) {
		NodeGroup node = this;
		do {
			node.flags.add( flag );
			node = node.getParent();
		}
		while( node != null );
	}

	/**
	 * @return Whether the transform has changed for this node
	 */
	protected boolean isDirtyTransform() {
		return false;
	}

	/**
	 * Maintains scene-graph structure and notifies parent nodes of changes.
	 */
	@Override
	public void setParent( NodeGroup parent ) {
		// Update scene-graph
		if( parent == null ) {
			// Remove from existing parent
			final NodeGroup current = this.getParent();
			if( current != null ) {
				current.children.remove( this );
				current.propagate( Flag.GRAPH );
			}
		}
		else {
			// Add to scene-graph
			verify( parent );
			parent.children.add( this );
			parent.propagate( Flag.GRAPH );
		}

		// Delegate
		super.setParent( parent );
	}

	/**
	 * Verifies this node can be added to the given parent.
	 * @param parent New parent node
	 */
	private void verify( NodeGroup parent ) {
		assert !parent.children.contains( this );
		if( this.getParent() != null ) throw new IllegalArgumentException( "Node already has a parent: " + this );
		if( parent == this ) throw new IllegalArgumentException( "Cannot add to self" );
		verifyNotCyclic( this, parent );
	}

	private static void verifyNotCyclic( NodeGroup node, NodeGroup parent ) {
		for( Node e : node.children ) {
			if( e == parent ) throw new IllegalArgumentException( "Cyclic dependency" );
			if( e instanceof NodeGroup ) {
				verifyNotCyclic( (NodeGroup) e, parent );
			}
		}
	}

	/**
	 * @return Children of this node
	 */
	public List<Node> getChildren() {
		return new ArrayList<>( children );
	}

	@Override
	public void accept( Visitor visitor ) {
		// Visit this node
		final boolean recurse = visitor.visit( this );

		// Recurse
		if( recurse ) {
			for( Node r : children ) {
				r.accept( visitor );
			}
		}
	}
}
