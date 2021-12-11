package org.sarge.jove.model;

import java.util.HashMap;
import java.util.Map;

/**
 * An <i>indexed builder</i> performs de-duplication of vertex data.
 * @author Sarge
 */
public class IndexedBuilder extends DefaultModel {
	private final Map<Vertex, Integer> map = new HashMap<>();

	/**
	 * Constructor.
	 * @param primitive Drawing primitive
	 */
	public IndexedBuilder(Primitive primitive) {
		super(primitive);
	}

	@Override
	public void add(Vertex v) {
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
	}
}
