package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Volume;

/**
 * A <i>node</i> is an element of a scene graph.
 * @author Sarge
 */
public class Node {
	private Node parent;
	private LocalTransform transform = new LocalTransform();
	private LocalMaterial mat = new LocalMaterial();
	private Volume vol = Volume.EMPTY;

	protected Node() {
	}

	/**
	 * Copy constructor.
	 * @param node Node to copy
	 */
	protected Node(Node node) {
		transform.set(node.transform.transform());
		mat.set(node.mat.material());
	}
	// TODO - separate helper?

	/**
	 * @return Parent of this node
	 */
	Node parent() {
		return parent;
	}

	/**
	 * @return Whether this is a root node
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Attaches this node to the given parent;
	 * @param parent New parent
	 * @throws IllegalStateException if this node is already attached
	 */
	protected void attach(Node parent) {
		if(!isRoot()) throw new IllegalStateException("Node is already attached: " + this);
		this.parent = parent;
		reset();
	}

	/**
	 * Detaches this node from its parent.
	 * @throws IllegalStateException if this node is not attached to a scene
	 */
	protected void detach() {
		if(isRoot()) throw new IllegalStateException("Node is not attached: " + this);
		parent = null;
	}

	/**
	 * Resets all properties of this node.
	 */
	private void reset() {
		transform.reset();
	}

	/**
	 * @return Material for this node
	 */
	public LocalMaterial material() {
		return mat;
	}

	/**
	 * @return Transform at this node
	 */
	public LocalTransform transform() {
		return transform;
	}

	/**
	 * @return Bounding volume of this node
	 */
	public Volume volume() {
		return vol;
	}

	/**
	 * Sets the bounding volume of this node.
	 * @param vol Bounding volume
	 */
	public void set(Volume vol) {
		this.vol = notNull(vol);
	}

	/**
	 * A <i>node visitor</i> defines an operation applied recursively to a scene graph.
	 */
	public interface Visitor {
		/**
		 * Visits the given node.
		 * @param node Node to visit
		 */
		void visit(Node node);
	}

	/**
	 * Applies the given visitor to this node.
	 * @param visitor Node visitor
	 */
	public void accept(Visitor visitor) {
		visitor.visit(this);
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
