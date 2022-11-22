package org.sarge.jove.scene.graph;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>group node</i> is a collection of nodes.
 * @author Sarge
 */
public class GroupNode extends Node {
	private final List<Node> nodes = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public GroupNode() {
	}

	/**
	 * Copy constructor.
	 * @param group Group node to copy
	 */
	protected GroupNode(GroupNode group) {
		super(group);
		for(Node n : group.nodes) {
			final Node clone = n.copy();
			nodes.add(clone);
		}
	}

	/**
	 * @return Children of this node
	 */
	public Stream<Node> nodes() {
		return nodes.stream();
	}

	/**
	 * Adds the given node to this group.
	 * @param node Node to add
	 * @throws IllegalStateException if the given node is already attached
	 * @throws IllegalArgumentException if the given node is this node
	 */
	public void add(Node node) {
		Check.notNull(node);
		if(node == this) throw new IllegalArgumentException("Cannot attach a node to itself");
		nodes.add(node);
		node.attach(this);
	}

	/**
	 * Removes the given node from this group.
	 * @param node Node to remove
	 * @throws IllegalStateException if the given node is not attached to this group
	 */
	public void remove(Node node) {
		final boolean removed = nodes.remove(node);
		if(!removed) throw new IllegalStateException("Not a child of this node");
		node.detach();
	}

	@Override
	public void accept(Visitor visitor) {
		// Visit this node
		visitor.visit(this);

		// Recurse to children
		for(Node n : nodes) {
			n.accept(visitor);
		}
	}

	@Override
	public GroupNode copy() {
		return new GroupNode(this);
	}

	/**
	 * Detaches all children from this group.
	 */
	public void clear() {
		for(Node n : nodes) {
			n.detach();
		}
		nodes.clear();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("children", nodes.size())
				.build();
	}
}
