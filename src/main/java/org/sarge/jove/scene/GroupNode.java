package org.sarge.jove.scene;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>group node</i> is a collection of nodes.
 * @author Sarge
 */
public class GroupNode extends AbstractNode {
	private final List<AbstractNode> nodes = new ArrayList<>();

	/**
	 * @return Child nodes of this group
	 */
	public Stream<? extends Node> stream() {
		return nodes.stream();
	}

	@Override
	public Stream<SceneGraph> nodes() {
		return nodes.stream().flatMap(SceneGraph::nodes);
	}

	/**
	 * Adds the given node to this group.
	 * @param node Node to add
	 * @throws IllegalStateException if the given node is already attached
	 * @throws IllegalArgumentException if the given node is this node
	 */
	public void add(AbstractNode node) {
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
	public void remove(AbstractNode node) {
		final boolean removed = nodes.remove(node);
		if(!removed) throw new IllegalStateException("Not a child of this node");
		node.detach();
	}

	/**
	 * Detaches all children from this group.
	 */
	public void clear() {
		for(AbstractNode n : nodes) {
			n.detach();
		}
		nodes.clear();
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
