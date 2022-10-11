package org.sarge.jove.scene;

import org.sarge.jove.geometry.Volume;

/**
 *
 * @author Sarge
 */
class LeafNode extends Node {
	@Override
	public void set(Volume vol) {
		if(vol instanceof AggregateVolume) throw new IllegalArgumentException("Aggregate volume cannot be applied to a leaf node: " + this);
		super.set(vol);
	}
}
