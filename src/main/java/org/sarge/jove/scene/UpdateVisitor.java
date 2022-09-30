package org.sarge.jove.scene;

import org.sarge.jove.scene.AbstractNode.Visitor;
import org.sarge.jove.scene.LocalMaterial.MaterialVisitor;
import org.sarge.jove.scene.LocalTransform.WorldMatrixVisitor;

/**
 * TODO
 * @author Sarge
 */
public class UpdateVisitor implements Visitor {
	private final WorldMatrixVisitor matrix = new WorldMatrixVisitor();
	private final MaterialVisitor mat = new MaterialVisitor();

	@Override
	public void visit(AbstractNode node) {
		matrix.update(node.transform());
		mat.update(node.material());
	}
}
