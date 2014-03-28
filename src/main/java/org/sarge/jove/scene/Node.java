package org.sarge.jove.scene;

import org.sarge.jove.geometry.Matrix;

/**
 * Scene-graph node.
 * @author Sarge
 */
public interface Node {
	/**
	 * Scene-graph visitor.
	 * @see #accept(Visitor)
	 */
	interface Visitor {
		/**
		 * Visits the given node.
		 * @param node Node being visited
		 * @return Whether to recurse to children of this element
		 */
		boolean visit( Node node );
	}

	/**
	 * @return Node name
	 */
	public String getName();

	/**
	 * @return Parent of this node
	 */
	NodeGroup getParent();

	/**
	 * @return Rendering queue for this node
	 */
	RenderQueue getRenderQueue();

	/**
	 * @return World transformation matrix for this node
	 */
	Matrix getWorldMatrix();

	/**
	 * Accepts the given scene-graph visitor.
	 * @param visitor Visitor
	 */
	void accept( Visitor visitor );

	/**
	 * Applies this nodes state changes.
	 * @param ctx Rendering context
	 */
	void apply( RenderContext ctx );

	/**
	 * Resets this nodes state changes.
	 * @param ctx Rendering context
	 */
	void reset( RenderContext ctx );
}
