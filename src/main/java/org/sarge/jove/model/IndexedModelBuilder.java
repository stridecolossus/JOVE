package org.sarge.jove.model;

import java.util.HashMap;
import java.util.Map;

/**
 * An <i>indexed model builder</i> performs de-duplication of vertex data.
 * @author Sarge
 */
public class IndexedModelBuilder extends ModelBuilder {
	private final Map<Vertex, Integer> map = new HashMap<>();

	@Override
	public IndexedModelBuilder add(Vertex v) {
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
