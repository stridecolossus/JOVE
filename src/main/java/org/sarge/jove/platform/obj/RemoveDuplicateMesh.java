package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * Adapter for an indexed model that performs de-duplication of the OBJ vertices.
 * @author Sarge
 */
class RemoveDuplicateMesh extends IndexedVertexMesh {
	private final Map<Vertex, Integer> map = new HashMap<>();

	public RemoveDuplicateMesh() {
		super(Primitive.TRIANGLE, List.of(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT));
	}

	@Override
	public RemoveDuplicateMesh add(Vertex vertex) {
		final Integer prev = map.get(vertex);
		if(prev == null) {
			// Register new vertex
			final Integer index = map.size();
			map.put(vertex, index);

			// Add vertex
			super.add(vertex);
			add(index);
		}
		else {
			// Otherwise add index for existing vertex
			add(prev);
		}

		return this;
	}
}
