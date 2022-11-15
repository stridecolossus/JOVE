package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.model.*;

/**
 * Adapter for an indexed model that performs vertex de-duplication of the OBJ vertices.
 * @author Sarge
 */
class RemoveDuplicateMesh extends IndexedMesh {
	private final Map<Vertex, Integer> map = new HashMap<>();

	public RemoveDuplicateMesh(Layout layout) {
		super(Primitive.TRIANGLE, layout);
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
