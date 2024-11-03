package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * A <i>node</i> is an element of a scene graph.
 * TODO
 * @author Sarge
 */
public class Node {
	private GroupNode parent;
	private LocalTransform transform = new LocalTransform();
	private LocalVolume vol = new LocalVolume();

	/**
	 * Constructor.
	 * @param parent Parent node
	 */
	protected Node(GroupNode parent) {
		this.parent = requireNonNull(parent);
		parent.attach(this);
	}

	/**
	 * Root node constructor.
	 */
	protected Node() {
	}

	/**
	 * @return Parent node or {@code null} is this is the root node
	 * @see #isRoot()
	 */
	public final Node parent() {
		return parent;
	}

	/**
	 * @return Root node
	 */
	public RootNode root() {
		return parent.root();
	}

	/**
	 * Detaches this node from the scene graph.
	 * @throws IllegalStateException if this is the root node
	 */
	public void detach() {
		if(parent == null) throw new IllegalStateException("Cannot detach the root node");
		parent.detach(this);
		parent = null;
	}

	/**
	 * @return Local transform at this node
	 */
	public final LocalTransform transform() {
		return transform;
	}

	/**
	 * @return Bounding volume of this node
	 */
	public final LocalVolume volume() {
		return vol;
	}

	@Override
	public int hashCode() {
		return Objects.hash(transform, vol);
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this;
	}
}
