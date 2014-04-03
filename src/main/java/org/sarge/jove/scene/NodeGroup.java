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
	 * Adds the given node to this group.
	 * @param node Node to add
	 * @throws IllegalArgumentException if the node cannot be added to this group
	 */
	void add( Node node ) {
		if( node.getParent() != null ) throw new IllegalArgumentException( "Node already has a parent: " + this );
		if( node == this ) throw new IllegalArgumentException( "Cannot add to self" );
		checkNotCyclic( node );

		children.add( node );
		propagate( Flag.GRAPH );
	}

	private void checkNotCyclic( Node node ) {
		if( node instanceof NodeGroup ) {
			final NodeGroup group = (NodeGroup) node;
			for( Node n : group.children ) {
				if( n == this ) throw new IllegalArgumentException( "Cyclic dependency" );
				checkNotCyclic( n );
			}
		}
	}

	/**
	 * Removes the given node from this group.
	 * @param node Node to remove
	 */
	void remove( Node node ) {
		children.remove( node );
		propagate( Flag.GRAPH );
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
