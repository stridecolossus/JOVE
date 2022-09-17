package org.sarge.jove.scene;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 *
 * @author Sarge
 */
public class GroupNode extends Node {
	private final List<Node> nodes = new ArrayList<>();

	/**
	 * @return Child nodes of this group
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
		if(node.parent != null) throw new IllegalStateException("Node is already attached: " + node);
		nodes.add(node);
		node.parent = this;
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
	public Stream<Renderable> render() {
		return nodes.stream().flatMap(Node::render);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), nodes);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof GroupNode that) &&
				super.isEqual(that) &&
				this.nodes.equals(that.nodes);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("children", nodes.size())
				.build();
	}
}
