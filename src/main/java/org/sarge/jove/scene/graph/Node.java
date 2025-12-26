package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;

/**
 * TODO
 * @author Sarge
 */
public class Node {
	private final String name;
	private final List<Node> children = new ArrayList<>();
	private Node parent;

	private LocalTransform transform = LocalTransform.NONE;
//	private Material material;
//	private Renderable renderable;
//	private RenderQueue queue;

	// TODO
	// - scenario test to flatten example scene graph -> render sequence

	/**
	 * Constructor.
	 * @param name			Node identifier
	 * @param parent		Parent node
	 */
	public Node(String name, Node parent) {
		this.name = requireNotEmpty(name);
		this.parent = requireNonNull(parent);
		parent.children.add(this);
	}

	/**
	 * Root node constructor.
	 */
	public Node(String name) {
		this.name = requireNotEmpty(name);
		this.parent = null;
	}

	/**
	 * @return Node identifier
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Parent of this node or {@code null} for a root node
	 */
	public Node parent() {
		return parent;
	}

	/**
	 * @return Children of this node
	 */
	public List<Node> children() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * @return Local transform
	 */
	public LocalTransform transform() {
		return transform;
	}

	/**
	 * Sets the local transform.
	 * @param transform Local transform
	 */
	public void transform(LocalTransform transform) {
		this.transform = requireNonNull(transform);
	}

	/**
	 * Clones this node as a new scene graph.
	 * @return Clone
	 */
	public Node copy() {
		// Clone this node
		final Node parent = new Node(name);
		parent.transform = new LocalTransform(transform);

		// Clone children
		for(Node n : children) {
			final Node child = n.copy();
			child.parent = parent;
			parent.children.add(child);
		}

		return parent;
	}

	/**
	 * Removes this node from the scene graph.
	 * @throws UnsupportedOperationException if this is a {@link #root(String)} node.
	 */
	public void remove() {
		if(parent == null) {
			throw new UnsupportedOperationException();
		}
		final boolean removed = parent.children.remove(this);
		assert removed;
		parent = null;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Node that) &&
				this.name.equals(that.name) &&
				(this.parent == that.parent) &&
				this.transform.equals(that.transform);
	}

	@Override
	public String toString() {
		return name;
	}
}
