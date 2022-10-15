package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.model.*;

/**
 * Adapter for an OBJ model builder that performs vertex de-duplication.
 * @author Sarge
 */
class DuplicateModel extends Model {
	private final Map<Vertex, Integer> map = new HashMap<>();

	public DuplicateModel() {
		super(Primitive.TRIANGLES);
	}

	@Override
	public DuplicateModel add(Vertex vertex) {
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
