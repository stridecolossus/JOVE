package org.sarge.jove.platform.obj;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.model.ModelBuilder;
import org.sarge.jove.model.Vertex;

/**
 * Adapter for an OBJ model builder that performs vertex de-duplication.
 * @author Sarge
 */
class DuplicateVertexModelBuilder extends ModelBuilder {
	private final Map<Vertex, Integer> map = new HashMap<>();

	@Override
	public DuplicateVertexModelBuilder add(Vertex v) {
		final Integer prev = map.get(v);
		if(prev == null) {
			// Register new vertex
			final Integer n = vertices.size();
			map.put(v, n);

			// Add vertex
			super.add(v);
			index.add(n);
		}
		else {
			// Otherwise add index for existing vertex
			index.add(prev);
		}

		return this;
	}
}
