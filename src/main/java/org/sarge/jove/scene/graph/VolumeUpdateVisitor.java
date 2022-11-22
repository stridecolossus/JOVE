package org.sarge.jove.scene.graph;

import org.sarge.jove.scene.graph.Node.Visitor;

public class VolumeUpdateVisitor implements Visitor {
	@Override
	public void visit(Node node) {
		// TODO - this sucks!?
		if((node instanceof GroupNode group) && (group.volume() instanceof AggregateVolume aggregate)) {

			// TODO
			// - iterate children
			// - get bounds of each volume
			// - sum largest
			// - create new volume from aggregate
			// - set this volume

		}
	}
}
