package org.sarge.jove.scene.graph;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>group node</i>
 * TODO
 * @author Sarge
 */
public class GroupNode extends Node {
	private final List<Node> nodes = new ArrayList<>();

	/**
	 * Constructor.
	 * @param parent Parent node
	 */
	public GroupNode(GroupNode parent) {
		super(parent);
	}

	/**
	 * Root node constructor.
	 */
	protected GroupNode() {
	}

	/**
	 * @return Child nodes
	 */
	public Stream<Node> nodes() {
		return nodes.stream();
	}

	/**
	 * Attaches the given node to this group.
	 * @param node Node to attach
	 */
	protected final void attach(Node node) {
		Check.notNull(node);
		assert !nodes.contains(node);
		nodes.add(node);
	}

	/**
	 * Detaches the given node from this group.
	 * @param node Node to detach
	 * @throws IllegalArgumentException if {@link #node} is not a member of this group
	 */
	protected void detach(Node node) {
		if(node.parent() != this) throw new IllegalArgumentException("Not a member of this group: " + node);
		Check.notNull(node);
		nodes.remove(node);
	}

	/**
	 * Removes all nodes from this group.
	 */
	public void clear() {
		for(Node n : new ArrayList<>(nodes)) {
			n.detach();
		}
		nodes.clear();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("nodes", nodes.size())
				.build();
	}
}
