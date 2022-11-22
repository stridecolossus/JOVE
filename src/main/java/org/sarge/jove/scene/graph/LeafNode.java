package org.sarge.jove.scene.graph;

import org.sarge.jove.scene.volume.Volume;

/**
 *
 * @author Sarge
 */
abstract class LeafNode extends Node {
	/**
	 * Constructor.
	 */
	protected LeafNode() {
	}

	/**
	 * Copy constructor.
	 * @param node Leaf node to copy
	 */
	protected LeafNode(LeafNode node) {
		super(node);
	}

	@Override
	public void set(Volume vol) {
		if(vol instanceof AggregateVolume) throw new IllegalArgumentException("Aggregate volume cannot be applied to a leaf node: " + this);
		super.set(vol);
	}
}
