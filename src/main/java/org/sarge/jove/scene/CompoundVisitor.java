package org.sarge.jove.scene;

import java.util.List;

import org.sarge.jove.scene.AbstractNode.Visitor;

/**
 * Compound visitor.
 * @author Sarge
 */
public class CompoundVisitor implements Visitor {
	/**
	 * Default visitor to update all aspects of a scene graph.
	 * @see WorldMatrixVisitor
	 */
	public static final CompoundVisitor DEFAULT = new CompoundVisitor(List.of(new WorldMatrixVisitor()));

	private final List<Visitor> visitors;

	/**
	 * Constructor.
	 * @param visitors Delegate visitors
	 */
	public CompoundVisitor(List<Visitor> visitors) {
		this.visitors = List.copyOf(visitors);
	}

	@Override
	public void visit(AbstractNode node) {
		for(Visitor e : visitors) {
			e.visit(node);
		}
	}
}
