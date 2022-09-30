package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Volume;
import org.sarge.lib.util.Utility;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNode implements Node {
	private AbstractNode parent;
	private Volume vol = Volume.EMPTY;
	private LocalTransform transform = new LocalTransform();
	private LocalMaterial mat = new LocalMaterial();

	/**
	 * @return Parent of this node
	 */
	AbstractNode parent() {
		return parent;
	}

	/**
	 * @return Whether this is a root node
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * @return Children of this node
	 */
	public abstract Stream<AbstractNode> nodes();

	/**
	 * Attaches this node to the given parent;
	 * @param parent New parent
	 * @throws IllegalStateException if this node is already attached
	 */
	protected void attach(AbstractNode parent) {
		assert isRoot();
		if(this.parent != null) throw new IllegalStateException("Node is already attached: " + this);
		this.parent = parent;
	}

	/**
	 * Detaches this node from its parent.
	 */
	protected void detach() {
		assert !isRoot();
		parent = null;
	}

	/**
	 * @return Material for this node
	 */
	public LocalMaterial material() {
		return mat;
	}

	// TODO - change in Node interface?
	@Override
	public LocalTransform transform() {
		return transform;
	}

	// TODO...

	@Override
	public Volume volume() {
		return vol;
	}

	/**
	 * Sets the bounding volume of this node.
	 * @param vol Bounding volume or {@link Volume#EMPTY} if none
	 */
	public void volume(Volume vol) {
		this.vol = notNull(vol);
	}

	// ...TODO

	/**
	 * A <i>node visitor</i> is used to apply some recursive operation to a scene graph.
	 */
	public interface Visitor {
		/**
		 * Applies this visitor to the given node.
		 * @param node Node
		 */
		void visit(AbstractNode node);
	}

	/**
	 * Recursively visits this node and its children.
	 * @param visitor Node visitor
	 */
	public void accept(Visitor visitor) {
		Utility.flatten(this, AbstractNode::nodes).forEach(visitor::visit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(transform, mat, vol);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(transform)
				.append(mat)
				.append(vol)
				.build();
	}
}
