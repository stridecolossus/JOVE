package org.sarge.jove.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * An <i>indexed model</i> is a model with indexed vertices.
 * @author Sarge
 */
public class IndexedModel extends Model {
	private final int[] indices;

	/**
	 * Constructor.
	 * @param model			Model
	 * @param indices		Indices
	 * @throws IllegalArgumentException if the number of vertices/indices is not valid for the drawing primitive
	 */
	public IndexedModel(Model model, int[] indices) {
		super(model);
		this.indices = Arrays.copyOf(indices, indices.length);
		if(!model.primitive().isValidVertexCount(indices.length)) throw new IllegalArgumentException("Invalid number of indices for primitive: " + this);
	}

	@Override
	public boolean isIndexed() {
		return true;
	}

	@Override
	public int length() {
		return indices.length;
	}

	@Override
	public Iterator<List<Vertex>> faces() {
		final var vertices = super.vertices();
		final var itr = Arrays.stream(indices).mapToObj(vertices::get).iterator();
		return new FaceIterator(itr);
	}

	/**
	 * @return Indices
	 */
	public IntStream indices() {
		return Arrays.stream(indices);
	}
}
