package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.Layout;

/**
 * An <i>indexed builder</i> performs de-duplication of vertex data.
 * @author Sarge
 */
public class IndexedBuilder extends ModelBuilder {
	private final Map<Vertex, Integer> map = new HashMap<>();
	private final List<Integer> index = new ArrayList<>();

	/**
	 * Constructor.
	 * @param layout Vertex layout
	 */
	public IndexedBuilder(List<Layout> layout) {
		super(layout);
	}

	@Override
	public ModelBuilder add(Vertex v) {
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

	@Override
	public Model build() {
		final int[] array = index.stream().mapToInt(Integer::intValue).toArray();
		return build(array.length, array);
	}
}
