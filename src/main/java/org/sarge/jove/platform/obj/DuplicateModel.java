package org.sarge.jove.platform.obj;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.model.MutableModel;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * Adapter for an OBJ model builder that performs vertex de-duplication.
 * @author Sarge
 */
class DuplicateModel extends MutableModel {
	private final Map<Vertex, Integer> map = new HashMap<>();

	public DuplicateModel() {
		primitive(Primitive.TRIANGLES);
	}

	@Override
	public DuplicateModel add(Vertex v) {
		final Integer prev = map.get(v);
		if(prev == null) {
			// Register new vertex
			final Integer index = map.size();
			map.put(v, index);

			// Add vertex
			super.add(v);
			add(index);
		}
		else {
			// Otherwise add index for existing vertex
			add(prev);
		}

		return this;
	}
}