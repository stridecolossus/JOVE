package org.sarge.jove.scene;

import org.sarge.jove.geometry.Matrix.Matrix4;
import org.sarge.jove.geometry.Transform;

/**
 * The <i>world matrix visitor</i> updates the world matrix of a scene graph.
 * @see AbstractNode#transform(Transform)
 * @author Sarge
 */
public class WorldMatrixVisitor implements AbstractNode.Visitor {
	private LocalTransform parent;
	private boolean dirty;

	@Override
	public void visit(AbstractNode node) {
		// Compose transform with parent
		final LocalTransform transform = node.transform();
		if(dirty || transform.isDirty()) {
			transform.update(parent);
			dirty = true;
		}

		// Record latest transform
		if(transform.transform() != Matrix4.IDENTITY) {
			parent = transform;
		}
	}
}
