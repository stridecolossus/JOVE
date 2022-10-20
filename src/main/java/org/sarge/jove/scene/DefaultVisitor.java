package org.sarge.jove.scene;

import org.sarge.jove.scene.Node.Visitor;

/**
 * The <i>default visitor</i> is used to update the properties of a scene graph after modification.
 * @author Sarge
 */
public class DefaultVisitor implements Visitor {
	private final Visitor transform = InheritedProperty.visitor(Node::transform);
	private final Visitor material = InheritedProperty.visitor(Node::material);
	// TODO - others: cull mode, bounding volume, others?
	// TODO - collection of inherited visitors?

	@Override
	public void visit(Node node) {
		transform.visit(node);
		material.visit(node);
		// TODO - update queues if material was dirty
	}
}
