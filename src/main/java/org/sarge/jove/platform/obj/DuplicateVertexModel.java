package org.sarge.jove.platform.obj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.MutableModel;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * Adapter for an OBJ model builder that performs vertex de-duplication.
 * @author Sarge
 */
class DuplicateVertexModel extends MutableModel {
	private final Map<Vertex, Integer> map = new HashMap<>();

	public DuplicateVertexModel() {
		super(Primitive.TRIANGLES, List.of(Point.LAYOUT, Vertex.NORMALS, Coordinate2D.LAYOUT));
	}

	@Override
	public DuplicateVertexModel add(Vertex v) {
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
