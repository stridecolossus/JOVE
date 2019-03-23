package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.sarge.jove.material.Material;

/**
 * A <i>node</i> is an element in a scene-graph.
 * @author Sarge
 */
public final class Node {
	/**
	 * Node visitor.
	 */
	@FunctionalInterface
	public interface Visitor {
		/**
		 * Visits the given node.
		 * @param node Node
		 * @return Whether to recurse to the children of the given node
		 */
		boolean visit(Node node);
	}

	// Graph
	private final String name;
	private Node parent;
	private final List<Node> children = new ArrayList<>();

	// Properties
	private LocalTransform transform = LocalTransform.none();
	private LocalVolume vol = LocalVolume.NONE;
	private Material mat = Material.NONE;
	private RenderQueue.Entry model = RenderQueue.Entry.NONE;

	/**
	 * Constructor.
	 * @param name Node name
	 */
	public Node(String name) {
		this.name = notEmpty(name);
	}

	/**
	 * @return Node name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Parent node or <tt>null</tt> for a root node
	 */
	public Node parent() {
		return parent;
	}

	/**
	 * @return Children of this node
	 */
	public Stream<Node> children() {
		return children.stream();
	}

	/**
	 * Adds a node to this scene-graph.
	 * @param child Node to add
	 * @throws IllegalArgumentException if the given node is a member of a scene-graph or is <i>this</i> node
	 */
	public void add(Node child) {
		if(child.parent != null) throw new IllegalArgumentException("Node is already a member of a scene-graph");
		if(child == this) throw new IllegalArgumentException("Cannot add self");
		child.parent = this;
		children.add(child);
	}

	/**
	 * Removes this node from the scene-graph.
	 * @throws IllegalStateException if this node is not in a scene-graph (i.e. is a root node)
	 */
	public void remove() {
		if(parent == null) throw new IllegalStateException("Cannot remove a root node");
		parent.children.remove(this);
		parent = null;
	}

	/**
	 * Removes <b>all</b> child nodes.
	 */
	public void clear() {
		for(Node child : children) {
			child.parent = null;
		}
		children.clear();
	}

	/**
	 * Accepts the given visitor.
	 * @param visitor Visitor
	 */
	public void accept(Visitor visitor) {
		// Visit this node
		final boolean recurse = visitor.visit(this);

		// Recurse to children
		if(recurse) {
			children.forEach(node -> node.accept(visitor));
		}
	}

	/**
	 * @return Node transform
	 */
	public LocalTransform transform() {
		return transform;
	}

	/**
	 * Sets the local transform at this node.
	 * @param transform Local transform
	 */
	public void transform(LocalTransform transform) {
		this.transform = notNull(transform);
	}

	/**
	 * @return Node bounding volume
	 */
	public LocalVolume volume() {
		return vol;
	}

	/**
	 * Sets the bounding volume for this node.
	 * @param vol Local bounding volume
	 */
	public void volume(LocalVolume vol) {
		this.vol = notNull(vol);
	}

	/**
	 * @return Whether this node is visible
	 * @see LocalVolume#isVisible()
	 */
	public boolean isVisible() {
		final boolean visible = vol.isVisible();
		if(parent == null) {
			return visible;
		}
		else {
			return visible && parent.isVisible();
		}
	}

	/**
	 * @return Material of this node or its ancestors
	 */
	public Material material() {
		Node node = this;
		while(true) {
			// Stop at inherited material
			if(node.mat != Material.NONE) {
				return node.mat;
			}

			// Otherwise walk to parent
			node = node.parent;

			// Stop at root node
			if(node == null) {
				return Material.NONE;
			}
		}
	}

	/**
	 * Sets the material for this node and its children.
	 * @param mat Material
	 */
	public void material(Material mat) {
		this.mat = notNull(mat);
	}

	/**
	 * @return Model at this node
	 */
	public RenderQueue.Entry model() {
		return model;
	}

	/**
	 * Sets the model entry at this node.
	 * @param entry Model entry
	 */
	public void model(RenderQueue.Entry entry) {
		// Remove previous entry
		if(this.model != RenderQueue.Entry.NONE) {
			this.model.queue().remove(this);
		}

		// Add new entry
		this.model = notNull(entry);
		if(entry != RenderQueue.Entry.NONE) {
			entry.queue().add(this);
		}
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this)
			.setExcludeFieldNames("parent", "children", "mat")
			.append("parent", parent == null ? "NULL" : parent.name)
			.append("mat", mat.name())
			.append("children", children.size())
			.build();
	}
}
